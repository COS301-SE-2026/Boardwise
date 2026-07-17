<template>
  <div>
    <ReaderToolbar 
      :rulebook="rulebook" 
      :current-page="currentPage" 
      :total-pages="localChunks.length" 
      :search-query="searchQuery"
      :match-count="matchResults.length"
      :current-match="currentMatch"
      :is-editing="isEditing"
      :is-saving="isSaving"
      :lock-held-by="lockHeldBy"
      :lock-expires-at="lockExpiresAt"
      :lock-error="lockError"
      :can-undo="canUndo"
      :can-redo="canRedo"
      @search="searchQuery = $event"
      @prev-match="prevMatch"
      @next-match="nextMatch"
      @clear-search="clearSearch"
      @edit="handleEdit"
      @toggle-history="showHistory = !showHistory"
      @undo="handleUndo"
      @redo="handleRedo"
    />

    <ReaderProgress :current-page="currentPage" :total-pages="localChunks.length" />

    <v-container fluid style="max-width: 1200px;">
      <v-row>
        <v-col cols="12" md="3">
          <ReaderSidebar 
            :pages="localChunks" 
            :current-page="currentPage" 
            :matching-chunks="matchingChunkIndices"
            @change="handlePageChange" 
          />
        </v-col>

        <v-col cols="12" md="9">
          <ReaderPage
            :rulebook="rulebook"
            :page="activeChunk"
            :is-first="currentPage === 0"
            :is-last="currentPage === localChunks.length - 1"
            :search-query="searchQuery"
            :active-occurrence="activeOccurrenceIndex"
            :is-editing="isEditing"
            :is-saving="isSaving"
            @prev="currentPage--"
            @next="currentPage++"
            @save="handleSave"
            @cancel="handleCancel"
          />
        </v-col>
      </v-row>
    </v-container>

    <ReaderHistory v-model="showHistory" :edits="editHistory" :is-loading="isLoadingHistory"/>

  </div>
</template>

<script setup>
import{ ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import ReaderToolbar from './ReaderToolbar.vue'
import ReaderProgress from './ReaderProgress.vue'
import ReaderSidebar from './ReaderSidebar.vue'
import ReaderPage from './ReaderPage.vue'
import ReaderHistory from './ReaderHistory.vue'

import { useEditLock } from '~/composables/useEditLock'
import { useEditHistory } from '~/composables/useEditHistory'
import { useSnackBar }  from '~/composables/useSnackbar'
import { useLibrary } from '~/composables/useLibrary'

const props = defineProps({
  rulebook: Object,
  chunks: { type: Array, default: () => [] }
})

const currentPage = ref(0)
const searchQuery = ref('')
const currentMatch = ref(0)

// edit logic

const localChunks = ref([...props.chunks])
const showHistory = ref<boolean>(false)

const { isEditing, isSaving, lockHeldBy, lockExpiresAt, lockError, canRedo, canUndo, currentVersion, startEditing, stopEditing, releaseAllLocks, commitDelta, undoEdit, redoEdit } = useEditLock()
const { editHistory, isLoadingHistory, fetchEditHistory } = useEditHistory()
const { show } = useSnackBar()
const { getRulebookText } = useLibrary()

// TODO: Integrate with backend to fetch the latest version and edits when the component is mounted or when the rulebook changes.
//const activeChunk = computed(() => props.chunks[currentPage.value])
const activeChunk = computed(() => localChunks.value[currentPage.value])

watch(() => props.chunks, (val) => {
  localChunks.value = [...val]
}, { immediate: true })

const handlePageChange = (index) => {
  if (isEditing.value) {
    show('Save or cancel your edits before switching sections.', 'info')
    return
  }
  currentPage.value = index
}

const handleEdit = async () => {
  if (!props.rulebook?.id) return
  await startEditing(props.rulebook.id)
  if (lockError.value) {
    show(lockError.value, 'error')
  }
}

const handleSave = async (deltaContent) => {
    if (!props.rulebook?.id) return;
    isSaving.value = true;

    const chunk = localChunks.value[currentPage.value];
    const previousContent = chunk?.content;

    localChunks.value[currentPage.value] = { ...chunk, content: deltaContent };

    try {
        const newVersion = await commitDelta(
            props.rulebook.id,
            chunk?.chunkId ?? '',
            deltaContent,
            currentVersion.value
        );
        currentVersion.value = newVersion;
        show('Section saved.', 'success');
    } catch(err) {
        if (err?.status === 409 && err?.data?.error === 'VersionMismatchException') {
            await reconcileStaleState();
        } else {
            localChunks.value[currentPage.value] = { ...chunk, content: previousContent };
            show('Failed to save. Please try again.', 'error');
        }
    } finally {
        await stopEditing(props.rulebook.id);
        isSaving.value = false;
    }
}

// History logic
watch(showHistory, async (val) => {
    if (val && props.rulebook?.id) {
        await fetchEditHistory(props.rulebook.id);
    }
})

// Cancel 
const handleCancel = async () => {
  if (!props.rulebook?.id) return
  await stopEditing(props.rulebook.id)
}

// Search logic 
const matchResults = computed(() => { 
  if(!searchQuery.value.trim()) return []
  const q = searchQuery.value.toLowerCase()
  const results = []

   props.chunks.forEach((chunk, chunkIndex) => {
    const content = chunk?.content?.toLowerCase() ?? ''
    let searchFrom = 0
    let occurrenceIndex = 0
    while (true) {
      const found = content.indexOf(q, searchFrom)
      if (found === -1) break
      results.push({ chunkIndex, occurrenceIndex })
      occurrenceIndex++
      searchFrom = found + 1
    }
   })

   return results
  })

const activeOccurrenceIndex = computed(() => {
  if (!matchResults.value.length) return -1
  return matchResults.value[currentMatch.value]?.occurrenceIndex ?? -1
})

const matchingChunkIndices = computed(() => 
  matchResults.value.map(m => m.chunkIndex)
)

const nextMatch = () => {
  if(!matchResults.value.length) return
  currentMatch.value = (currentMatch.value + 1) % matchResults.value.length
  currentPage.value = matchResults.value[currentMatch.value].chunkIndex
}

const prevMatch = () => {
  if (!matchResults.value.length) return 
  currentMatch.value = (currentMatch.value - 1 + matchResults.value.length) % matchResults.value.length
  currentPage.value = matchResults.value[currentMatch.value].chunkIndex
}

const clearSearch = () => {
  searchQuery.value = ''
  currentMatch.value = 0
}

watch(searchQuery, ()=> {
  currentMatch.value = 0
  if (matchResults.value.length) {
    currentPage.value = matchResults.value[0].chunkIndex
  }
})


const reconcileStaleState = async () => {
    if (!props.rulebook?.id) return;
    try {
        const fresh = await getRulebookText(props.rulebook.id);
        if (fresh?.chunks)  localChunks.value = [...fresh.chunks];
        if (fresh?.version) currentVersion.value = fresh.version;
        show('Your view was out of date and has been refreshed.', 'info');
    } catch {
        show('Failed to refresh document state.', 'error');
    }
}

const handleUndo = async () => {
    if (!props.rulebook?.id) return;
    try {
        const newVersion = await undoEdit(props.rulebook.id, '', currentVersion.value);
        currentVersion.value = newVersion;
    } catch(err) {
        if (err?.status === 409) {
            show('Nothing left to undo.', 'info');
        } else {
            show('Undo failed.', 'error');
        }
    }
}

const handleRedo = async () => {
    if (!props.rulebook?.id) return;
    try {
        const newVersion = await redoEdit(props.rulebook.id, '', currentVersion.value);
        currentVersion.value = newVersion;
    } catch(err) {
        if (err?.status === 409) {
            show('Nothing left to redo.', 'info');
        } else {
            show('Redo failed.', 'error');
        }
    }
}

const handleBeforeUnload = (e) => {
    if (isEditing.value && props.rulebook?.id) {
        releaseAllLocks();
        e.preventDefault();
        e.returnValue = '';
    }
}

onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload);
})

onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload);
    if (isEditing.value && props.rulebook?.id) {
        releaseAllLocks();
    }
})

onBeforeRouteLeave(async () => {
    if (isEditing.value && props.rulebook?.id) {
        await stopEditing(props.rulebook.id);
    }
})

</script>
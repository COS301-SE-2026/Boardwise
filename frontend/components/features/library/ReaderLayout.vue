<template>
  <div>
    <ReaderToolbar 
      :rulebook="rulebook" 
      :current-page="activeChunkIndex"
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

    <ReaderProgress :current-page="activeChunkIndex" :total-pages="localChunks.length" />

    <v-container fluid style="max-width: 1200px;">
      <v-row>
        <v-col cols="12" md="3">
          <ReaderSidebar 
            :pages="localChunks"
            :current-page="activeChunkIndex"
            :matching-chunks="matchingChunkIndices"
            @change="handlePageChange" 
          />
        </v-col>

        <v-col cols="12" md="9">
          <v-card rounded="xl" elevation="1" class="pb-10">
            <!-- Document Header -->
            <BaseImage :src="rulebook?.coverUrl" :alt="rulebook?.title" height="280px" fit="cover" />

            <div class="pa-10 pt-10 pb-2">
              <p class="text-caption text-uppercase font-weight-bold text-primary mb-2">
                {{ formattedGenres }}
              </p>

              <h1 class="text-h4 font-weight-bold mb-4">{{ rulebook?.title }}</h1>

              <div class="d-flex flex-wrap ga-4 mb-2">
                <v-chip size="small" prepend-icon="mdi-account-group">{{ formattedPlayerCount }}</v-chip>
                <v-chip size="small" prepend-icon="mdi-clock-outline">{{ rulebook?.duration }}</v-chip>
                <v-chip size="small" prepend-icon="mdi-account">{{ rulebook?.minAge }}</v-chip>
              </div>

              <v-divider class="my-7 mb-6" />
            </div>

            <!-- Continuous Editor Canvas -->
             <div class="px-10">
              <transition-group name="block-list" tag="div" class="blocks-wrapper d-flex flex-column ga-4">
                <ReaderBlock
                  v-for="(chunk, i) in localChunks"
                  :key="chunk.chunkId || i"
                  :data-index="i"
                  :ref="(el) => setBlockRef(el, i)"
                  :chunk="chunk"
                  :index="i"
                  :is-editing="isEditing"
                  :is-saving="isSaving"
                  :search-query="searchQuery"
                  :active-occurrence="activeOccurrenceIndex"
                  @save="handleSave"
                  @cancel="handleCancel"
                  @delete="handleDelete"
                  @insert="handleInsert"
                />
              </transition-group>
            </div>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <ReaderHistory
      :model-value="showHistory"
      @update:model-value="showHistory = $event"
      :edits="editHistory"
      :is-loading="isLoadingHistory"
      :error="historyError"
    />

    <AIFloatingButton @click="showRagPanel = true" />
    <RagPanel v-model="showRagPanel" :rulebook="rulebook" />

  </div>
</template>

<script setup>
import{ ref, computed, watch, onMounted, onUnmounted, provide } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import ReaderToolbar from './ReaderToolbar.vue'
import ReaderProgress from './ReaderProgress.vue'
import ReaderSidebar from './ReaderSidebar.vue'
import ReaderBlock from './ReaderBlock.vue'
import ReaderHistory from './ReaderHistory.vue'

import BaseImage from '~/components/ui/BaseImage.vue'

import AIFloatingButton from '~/components/layout/AIFloatingButton.vue'
import RagPanel from '../rag/RagPanel.vue'

import { useEditLock } from '~/composables/useEditLock'
import { useEditHistory } from '~/composables/useEditHistory'
import { useSnackBar }  from '~/composables/useSnackbar'
import { useLibrary } from '~/composables/useLibrary'

const props = defineProps({
  rulebook: Object,
  chunks: { type: Array, default: () => [] }
})

const activeChunkIndex = ref(0)
const blockRefs = ref([])
const searchQuery = ref('')
const currentMatch = ref(0)
const showRagPanel = ref(false)

const setBlockRef = (el, index) => {
  if(el){
    blockRefs.value[index] = el.$el || el;
  }
}

let observer = null;

// edit logic

const localChunks = ref([...props.chunks])
const showHistory = ref(false)

const { isEditing, isSaving, lockHeldBy, lockExpiresAt, lockError, canRedo, canUndo, currentVersion, startEditing, stopEditing, releaseAllLocks, commitDelta, undoEdit, redoEdit, insertChunk, deleteChunk } = useEditLock()
const { editHistory, isLoadingHistory, historyError, fetchEditHistory } = useEditHistory()
const { show } = useSnackBar()
const { getRulebookText } = useLibrary()


watch(() => props.chunks, (val) => {
  localChunks.value = [...val]
  if(activeChunkIndex.value >= localChunks.value.length) {
    activeChunkIndex.value = Math.max(0, localChunks.value.length - 1)
  }
}, { immediate: true, deep: true })

const handlePageChange = (index) => {
  if (isEditing.value) {
    show('Save or cancel your edits before switching sections.', 'info')
    return
  }
  const targetBlock = blockRefs.value[index];
  if(targetBlock){
    targetBlock.scrollIntoView({behavior: 'smooth', block: 'center'});
  }
}

provide('jumpToSection', handlePageChange)

const handleDelete = async (chunkId) => {
  if(!props.rulebook?.id) return
  isSaving.value = true;

  try{
    const newVersion = await deleteChunk(
      props.rulebook.id,
      chunkId,
      currentVersion.value
    )
    currentVersion.value = newVersion;
    show('Section deleted.', 'success')

    if(document.activeElement instanceof HTMLElement){
      document.activeElement.blur();
    }
  } catch(err){
    if(err?.status === 409 && err?.data?.error === 'VersionMismatchException'){
    await reconcileStaleState()
    } else{
      show('Failed to delete section', 'error')
    }
  } finally {
    await stopEditing(props.rulebook.id)
    isSaving.value = false
  }
}

const handleInsert = async (targetIndex) => {
  if(!props.rulebook?.id) return;
  isSaving.value = true

  try{
    const newVersion = await insertChunk(
      props.rulebook.id,
      "New Section Content...",
      targetIndex,
      currentVersion.value
    )

    currentVersion.value = newVersion;
    show('New section added.', 'success')

    await nextTick();
    const targetBlock = blockRefs.value[targetIndex];
    if(targetBlock){
      const textarea = targetBlock.querySelector('textarea');
      if(textarea){
        textarea.focus();
      }
      targetBlock.scrollIntoView({behavior: 'smooth', block:'center'});
    }
  }catch (err) {
    if(err?.status === 409 && err?.data?.error === 'VersionMismatchException') {
      await reconcileStaleState();
    } else {
      show('Failed to add section.', 'error')
    }
  } finally {
    await stopEditing(props.rulebook.id);
    isSaving.value = false;
  }
}

const handleEdit = async () => {
  if (!props.rulebook?.id) return;
  await startEditing(props.rulebook.id);
  if (lockError.value) {
    show(lockError.value, 'error');
  }
}

const handleSave = async ({chunkId, content}) => {
    if (!props.rulebook?.id) return;
    isSaving.value = true;

  ;
    const chunk = localChunks.value.find(c => c.chunkId === chunkId);
    const previousContent = chunk?.content;
    if(chunk) chunk.content = content;

    try {
        const newVersion = await commitDelta(
            props.rulebook.id,
            chunkId,
            content,
            currentVersion.value
        );
        currentVersion.value = newVersion;
        show('Section saved.', 'success');
    } catch(err) {
        if (err?.status === 409 && err?.data?.error === 'VersionMismatchException') {
            await reconcileStaleState();
        } else {
            if(chunk) chunk.content = previousContent;
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
        if(historyError.value){
          show(historyError.value, 'error');
        }
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

   localChunks.value.forEach((chunk, chunkIndex) => {
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
  
  const targetIndex = matchResults.value[currentMatch.value].chunkIndex;
  handlePageChange(targetIndex);
}

const prevMatch = () => {
  if (!matchResults.value.length) return 
  currentMatch.value = (currentMatch.value - 1 + matchResults.value.length) % matchResults.value.length
  
  const targetIndex = matchResults.value[currentMatch.value].chunkIndex;
  handlePageChange(targetIndex);
}

const clearSearch = () => {
  searchQuery.value = ''
  currentMatch.value = 0
}

watch(searchQuery, ()=> {
  currentMatch.value = 0
  if (matchResults.value.length) {
    handlePageChange(matchResults.value[0].chunkIndex);
  }
})


const reconcileStaleState = async (silent = false) => {
    if (!props.rulebook?.id) return;
    try {
        const fresh = await getRulebookText(props.rulebook.id);
        if (fresh?.chunks)  localChunks.value = [...fresh.chunks];
        if (fresh?.version) currentVersion.value = fresh.version;
        if(!silent) show('Your view was out of date and has been refreshed.', 'info');
    } catch {
        show('Failed to refresh document state.', 'error');
    }
}

const handleUndo = async () => {
    if (!props.rulebook?.id) return;
    try {
        const newVersion = await undoEdit(props.rulebook.id, '', currentVersion.value);
        currentVersion.value = newVersion;
        await reconcileStaleState(true);
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
        await reconcileStaleState(true);
    } catch(err) {
        if (err?.status === 409) {
            show('Nothing left to redo.', 'info');
        } else {
            show('Redo failed.', 'error');
        }
    }
}

const formattedPlayerCount = computed(() => {
  if(!props.rulebook) return '0 players';

  const min = props.rulebook.minPlayers;
  const max = props.rulebook.maxPlayers;

  if(min == max){
    return `${min} players`;
  }
  return `${min} - ${max} players`;
});

const formattedGenres = computed(() => {
  return props.rulebook?.genres?.join(', ') ?? '';
});

const handleBeforeUnload = (e) => {
    if (isEditing.value && props.rulebook?.id) {
        releaseAllLocks();
        e.preventDefault();
        e.returnValue = '';
    }
}

onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload);
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if(entry.isIntersecting){
          const index = Number(entry.target.dataset.index);
          if(activeChunkIndex.value !== index){
            activeChunkIndex.value = index;
          }
        }
      });
    }, {
      rootMargin: "-40% 0px -40% 0px",
      threshold: 0
    });

    nextTick(() => {
      blockRefs.value.forEach(block => {
        if(block) observer.observe(block);
      });
    });
})

onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload);
    if (isEditing.value && props.rulebook?.id) {
        releaseAllLocks();
    }
    if(observer) observer.disconnect();
})

onBeforeRouteLeave(async () => {
    if (isEditing.value && props.rulebook?.id) {
        await stopEditing(props.rulebook.id);
    }
})

defineExpose({
  reconcileStaleState
});

</script>
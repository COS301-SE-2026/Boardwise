<template>
  <div>
    <ReaderToolbar 
      :rulebook="rulebook" 
      :current-page="currentPage" 
      :total-pages="chunks.length" 
      :search-query="searchQuery"
      :match-count="matchResults.length"
      :current-match="currentMatch"
      :is-editing="isEditing"
      :is-saving="isSaving"
      :lock-held-by="lockHeldBy"
      :lock-expires-at="lockExpiresAt"
      :lock-error="lockError"
      @search="searchQuery = $event"
      @prev-match="prevMatch"
      @next-match="nextMatch"
      @clear-search="clearSearch"
      @edit="handleEdit"
    />

    <ReaderProgress :current-page="currentPage" :total-pages="chunks.length" />

    <v-container fluid style="max-width: 1200px;">
      <v-row>
        <v-col cols="12" md="3">
          <ReaderSidebar 
            :pages="chunks" 
            :current-page="currentPage" 
            :matching-chunks="matchingChunkIndices"
            @change="currentPage = $event" />
        </v-col>

        <v-col cols="12" md="9">
          <ReaderPage
            :rulebook="rulebook"
            :page="activeChunk"
            :is-first="currentPage === 0"
            :is-last="currentPage === chunks.length - 1"
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
  </div>
</template>

<script setup>
import{ ref, computed, watch } from 'vue'
import ReaderToolbar from './ReaderToolbar.vue'
import ReaderProgress from './ReaderProgress.vue'
import ReaderSidebar from './ReaderSidebar.vue'
import ReaderPage from './ReaderPage.vue'

import { useEditLock } from '~/composables/useEditLock'
import { useSnackBar }  from '~/composables/useSnackbar'

const props = defineProps({
  rulebook: Object,
  chunks: { type: Array, default: () => [] }
})

const currentPage = ref(0)
const searchQuery = ref('')
const currentMatch = ref(0)

// edit logic

const localChunks  = ref([...props.chunks])
const version      = ref(0) // TODO: seed from response.data.version when fetching text

const { isEditing, isSaving, lockHeldBy, lockExpiresAt, lockError, startEditing, stopEditing, commitDelta } = useEditLock()
const { show } = useSnackBar()

const activeChunk = computed(() => props.chunks[currentPage.value])

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
  if (!props.rulebook?.id) return
  isSaving.value = true

  try {
    const chunk = localChunks.value[currentPage.value]

    // Update local chunk immediately so UI reflects the change
    localChunks.value[currentPage.value] = {
      ...chunk,
      content: deltaContent
    }

    const newVersion = await commitDelta(
      props.rulebook.id,
      chunk.chunkId,
      deltaContent,
      version.value
    )

    version.value = newVersion
    show('Section saved.', 'success')
  } catch (err) {
    show('Failed to save. Please try again.', 'error')
    console.error('Save error:', err)
  } finally {
    await stopEditing(props.rulebook.id)
  }
}

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
</script>
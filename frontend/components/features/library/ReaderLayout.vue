<template>
  <div>
    <ReaderToolbar 
      :rulebook="rulebook" 
      :current-page="currentPage" 
      :total-pages="chunks.length" 
      :search-query="searchQuery"
      :match-count="matchResults.length"
      :current-match="currentMatch"
      @search="searchQuery = $event"
      @prev-match="prevMatch"
      @next-match="nextMatch"
      @clear-search="clearSearch"
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
            @prev="currentPage--"
            @next="currentPage++"
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

const props = defineProps({
  rulebook: Object,
  chunks: { type: Array, default: () => [] }
})

const currentPage = ref(0)
const searchQuery = ref('')
const currentMatch = ref(0)

const activeChunk = computed(() => props.chunks[currentPage.value])

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
<template>
  <div>
    <ReaderToolbar :rulebook="rulebook" :current-page="currentPage" :total-pages="chunks.length" />
    <ReaderProgress :current-page="currentPage" :total-pages="chunks.length" />

    <v-container fluid style="max-width: 1200px;">
      <v-row>
        <v-col cols="12" md="3">
          <ReaderSidebar :pages="chunks" :current-page="currentPage" @change="currentPage = $event" />
        </v-col>

        <v-col cols="12" md="9">
          <ReaderPage
            :rulebook="rulebook"
            :page="activeChunk"
            :is-first="currentPage === 0"
            :is-last="currentPage === chunks.length - 1"
            @prev="currentPage--"
            @next="currentPage++"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import ReaderToolbar from './ReaderToolbar.vue'
import ReaderProgress from './ReaderProgress.vue'
import ReaderSidebar from './ReaderSidebar.vue'
import ReaderPage from './ReaderPage.vue'

const props = defineProps({
  rulebook: Object,
  chunks: { type: Array, default: () => [] }
})

const currentPage = ref(0)

const activeChunk = computed(() => props.chunks[currentPage.value])
</script>
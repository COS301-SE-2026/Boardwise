<template>
  <div class="reader-page">

    <ReaderToolbar
      :rulebook="rulebook"
      :current-page="currentPage"
    />

    <ReaderProgress
      :current-page="currentPage"
      :total-pages="rulebook?.pages?.length ?? 0"
    />

    <div class="reader-layout">

      <ReaderSidebar
        :pages="rulebook?.pages ?? []"
        :current-page="currentPage"
        @change="currentPage = $event"
      />

      <div class="main">
        <ReaderPage
          :rulebook="rulebook"
          :page="activePage"
          :is-first="currentPage === 0"
          :is-last="currentPage === (rulebook?.pages?.length - 1)"
          @prev="currentPage--"
          @next="currentPage++"
        />
      </div>

    </div>

  </div>
</template>

<script setup>
import ReaderToolbar from './ReaderToolbar.vue'
import ReaderProgress from './ReaderProgress.vue'
import ReaderSidebar from './ReaderSidebar.vue'
import ReaderPage from './ReaderPage.vue'

const props = defineProps({
  rulebook: Object
})

const currentPage = ref(0)

const activePage = computed(() =>
  props.rulebook?.pages?.[currentPage.value]
)
</script>

<style scoped>
.reader-page {
  min-height: 100vh;
  background: #f6f6f6;
}

.reader-layout {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  align-items: flex-start;
}

.main { flex: 1; }

@media (max-width: 900px) {
  .reader-layout {
    flex-direction: column;
    padding: 16px;
  }
}
</style>
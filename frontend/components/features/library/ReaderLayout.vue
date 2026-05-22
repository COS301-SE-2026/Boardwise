<template>
  <div>

    <ReaderToolbar
      :rulebook="rulebook"
      :current-page="currentPage"
    />

    <ReaderProgress
      :current-page="currentPage"
      :total-pages="rulebook?.pages?.length ?? 0"
    />

    <v-container fluid style="max-width: 1200px;">
      <v-row>
        <v-col cols="12" md="3">
          <ReaderSidebar
            :pages="rulebook?.pages ?? []"
            :current-page="currentPage"
            @change="currentPage = $event"
          />
        </v-col>

        <v-col cols="12" md="9">
          <ReaderPage
            :rulebook="rulebook"
            :page="activePage"
            :is-first="currentPage === 0"
            :is-last="currentPage === (rulebook?.pages?.length ?? 1) - 1"
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
  rulebook: Object
})

const currentPage = ref(0)

const activePage = computed(() =>
  props.rulebook?.pages?.[currentPage.value]
)
</script>
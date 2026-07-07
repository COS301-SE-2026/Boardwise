<template>
  <v-app-bar flat border="b" color="surface" height="64" style="position: sticky; top: 0; z-index: 20;">

    <NuxtLink :to="`/library`" class="text-primary font-weight-bold text-decoration-none ml-4">
      <v-icon start>mdi-arrow-left</v-icon>
      Back
    </NuxtLink>

    <v-app-bar-title class="text-body-1 font-weight-medium">
      {{ rulebook?.title }}
    </v-app-bar-title>

    <v-tooltip text="Download PDF" location="bottom">
      <template #activator="{ props: tooltipProps }">
        <v-btn
          v-bind="tooltipProps"
          icon
          size="small"
          variant="text"
          :loading="isDownloading"
          :disabled="!rulebook?.id"
          @click="handleDownload"
        >
          <v-icon>mdi-download</v-icon>
        </v-btn>
      </template>
    </v-tooltip>

    <template #append>
      <div class="d-flex align-center ga-2 mr-4">

        <template v-if="showSearch">
          <BaseSearch 
            v-model="localQuery"
            placeholder="Search in this rulebook"
            autofocus
            style="width: 220px;"
            @update:model-value="emit('search', $event)"
            @keydown.enter="$emit('next-match')"
            @keydown.escape="closeSearch"
          />

          <span v-if="localQuery" class="text-caption text-medium-emphasis text-no-wrap">
            {{ matchCount > 0 ? `${currentMatch + 1} / ${matchCount}`: 'No matches' }}
          </span>

          <v-btn icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('prev-match')">
            <v-icon size="18">mdi-chevron-up</v-icon>
          </v-btn>

          <v-btn icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('next-match')">
            <v-icon size="18">mdi-chevron-down</v-icon>
          </v-btn>

          <v-btn icon size="small" variant="text" @click="closeSearch">
            <v-icon size="18">mdi-close</v-icon>
          </v-btn>
        </template>

        <v-btn icon size="small" variant="text" @click="showSearch = !showSearch">
          <v-icon>mdi-magnify</v-icon>
        </v-btn>

        <span class="text-caption text-medium-emphasis text-no-wrap">
          {{ currentPage + 1 }} / {{ totalPages }}
        </span>

      </div>
    </template>

  </v-app-bar>
</template>

<script setup>
import { ref } from 'vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'

const props = defineProps({
  rulebook: Object,
  currentPage: Number,
  totalPages: Number,
  searchQuery: { type: String, default: ''},
  matchCount: { type: Number, default: 0},
  currentMatch: { type: Number, default: 0},
})

const emit = defineEmits(['search', 'next-match', 'prev-match', 'clear-search'])

const showSearch = ref(false)
const localQuery = ref('')

const closeSearch = () => {
  showSearch.value = false
  emit('clear-search')
}

const isDownloading = ref(false)

const handleDownload = async () => {
  if (!props.rulebook?.id) return
  isDownloading.value = true

  try {
    // TODO: replace with real API call
    await new Promise(resolve => setTimeout(resolve, 800))
    console.log('Download requested for:', props.rulebook.id)
  } finally {
    isDownloading.value = false
  }
}

</script>
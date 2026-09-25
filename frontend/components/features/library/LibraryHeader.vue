<template>
  <PageHeader
    data-test="library-header"
    title="Library"
    subtitle="Browse community rulebooks"
    action-label="Upload Rulebook"
    action-icon="mdi-upload"
    @action="$emit('upload')"
  >
    <template #search>
      <RulebookSearch
        @data-test="rulebook-search"
        @search="$emit('search', $event)" 
      />
    </template>
  </PageHeader>
</template>

<script setup>
import PageHeader from '~/components/layout/PageHeader.vue'
import RulebookSearch from './RulebookSearch.vue'

defineEmits(['search', 'upload'])
</script>
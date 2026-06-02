<template> 
    <div class="d-flex flex-column ga-5">
    <SectionTitle title="Library" subtitle="Browse rulebooks and guides" />
    <RulebookSearch @search="$emit('search', $event)" @upload="$emit('upload')" />
    <RulebookTabs />
  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import RulebookSearch from './RulebookSearch.vue'
import RulebookTabs from './RulebookTabs.vue'

defineEmits(['search', 'upload'])
</script>

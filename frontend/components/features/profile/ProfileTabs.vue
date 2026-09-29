<template>
  <BaseTabs
    :tabs="tabs"
    :model-value="activeTab" 
    color="primary" 
    @update:model-value="$emit('change', $event)">
  </BaseTabs>
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue';

defineProps({
  activeTab: String
})

defineEmits(['change'])

const tabs = ['Games Owned', 'Listings']
</script>
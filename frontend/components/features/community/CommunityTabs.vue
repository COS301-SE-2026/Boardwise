<template>
  <BaseTabs
    :tabs="tabs"
    :active-tab="activeTab"
    @change="$emit('change', $event)"
  />
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue'

defineProps({
  activeTab: {
    type: String,
    default: 'Collection'
  }
})

defineEmits(['change'])

const tabs = ['Collection', 'Events', 'Members', 'Discussion', 'About']
</script>

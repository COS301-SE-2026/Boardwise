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
    activeTab: String
})

defineEmits(['change'])

const tabs = [
  'Games Owned',
  'Listings',
]
</script>
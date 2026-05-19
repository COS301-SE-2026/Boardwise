<template>
  <BaseTabs
    :tabs="tabs"
    :active-tab="selectedTab"
    @change="selectedTab = $event"
  />
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue'

const tabs = ['All', 'Strategy', 'Family', 'Party']
const selectedTab = ref('All')
</script>
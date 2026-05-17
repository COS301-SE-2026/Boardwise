<template>
  <BaseTabs
    :tabs="tabs"
    v-model="selectedTab"
  />
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue'

const tabs = [
  'Community',
  'Browse'
]

const selectedTab = ref('Trending')
</script>
<template>
  <BaseTabs
    :tabs="tabs"
    v-model="selectedTab"
  />
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue'

const tabs = [
  'Trending',
  'Nearby',
  'New'
]

const selectedTab = ref('Trending')
</script>
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
  'Web'
]

const selectedTab = ref('Community')
</script>
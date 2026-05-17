<template>
  <BaseTabs
    :tabs="tabs"
    v-model="selectedTab"
  />
</template>

<script setup>
import BaseTabs from '~/components/ui/BaseTabs.vue'

const tabs = [
  'Public',
  'Private',
  'New'
]

const selectedTab = ref('Public')
</script>
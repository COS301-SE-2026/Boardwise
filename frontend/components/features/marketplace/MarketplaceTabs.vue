<template>
  <v-tabs v-model="selectedTab" color="primary">
    <v-tab v-for="tab in tabs" :key="tab" :value="tab">
      {{ tab }}
    </v-tab>
  </v-tabs>
</template>

<script setup>

const tabs = [
  'Community Listings',
  // 'Web'
]

const selectedTab = ref('Community Listings')
</script>
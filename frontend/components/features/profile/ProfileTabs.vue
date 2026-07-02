<template>
  <v-tabs :model-value="activeTab" color="primary" @update:model-value="$emit('change', $event)">
    <v-tab v-for="tab in tabs" :key="tab" :value="tab">{{ tab }}</v-tab>
  </v-tabs>
</template>

<script setup>
defineProps({
  activeTab: String
})

defineEmits(['change'])

const tabs = ['Games Owned', 'Listings']
</script>
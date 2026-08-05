<template>
  <v-btn-toggle
    :model-value="activeTab"
    @update:model-value="$emit('change', $event)"
  >
    <v-btn
      v-for="tab in tabs"
      :key="tab"
      :value="tab"
    >
      {{ tab }}
    </v-btn>
  </v-btn-toggle>
</template>

<script setup>
import BaseButton from './BaseButton.vue'

defineProps({
  tabs: Array,
  activeTab: String
})

defineEmits(['change'])
</script>
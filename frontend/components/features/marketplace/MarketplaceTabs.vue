<template>
  <v-tabs :model-value="modelValue" color="primary" @update:model-value="$emit('update:modelValue', $event)">
    <v-tab v-for="tab in tabs" :key="tab" :value="tab">
      {{ tab }}
    </v-tab>
  </v-tabs>
</template>

<script setup>

defineProps({
  modelValue: {
    type: String,
    default: 'Community Listings'
  }
})

defineEmits(['update:modelValue'])

const tabs = [
  'Community Listings',
  'Web'
]
</script>
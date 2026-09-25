<template>
  <BaseTabs 
    data-test="social-tabs" 
    :active-tab="modelValue" 
    :tabs="tabs"
    aria-label="Explore sections"
    @change="$emit('update:modelValue', $event)"
  />
</template>

<script setup>

import BaseTabs from '~/components/ui/BaseTabs.vue'

defineProps({
  modelValue: {
    type: String,
    default: 'Friends'
  }
})

defineEmits(['update:modelValue'])

const tabs = ['Communities', 'Friends']
</script>
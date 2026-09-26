<template>
  <BaseTabs 
    data-test="marketplace-tabs" 
    :model-value="modelValue" 
    :tabs="tabs"
    color="primary"
    @update:model-value="$emit('update:modelValue', $event)"
    >
  </BaseTabs>
</template>

<script setup>

import BaseTabs from '~/components/ui/BaseTabs.vue';
defineProps({
  modelValue: {
    type: String,
    default: 'Community Listings'
  }
})

defineEmits(['update:modelValue'])

const tabs = [
  'Community Listings',
  'Web Listings'
]
</script>
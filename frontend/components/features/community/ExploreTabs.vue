<template>
  <BaseTabs
    :tabs="tabs"
    :active-tab="activeTab"
    @change="$emit('change', $event)"
  />
</template>

<script setup>
import { type } from 'happy-dom/lib/PropertySymbol';
import BaseTabs from '~/components/ui/BaseTabs.vue'

defineProps({
  activeTab: {
    type: String,
    default: 'Chat'
  }
})
 
defineEmits(['change'])
 
const tabs = ['Chat', 'Members', 'About']

</script>
<template>
  <div class="pa-6">
    <div class="d-flex justify-end mb-4">
      <BaseButton variant="secondary" prepend-icon="mdi-close" @click="$emit('close')">
        Close
      </BaseButton>
    </div>
    <RulebookDetailsHero :rulebook="rulebook" />
  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import RulebookDetailsHero from './RulebookDetailsHero.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])
</script>
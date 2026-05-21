<template>
  <v-dialog
    :model-value="modelValue"
    max-width="1200"
    scrollable
    @update:model-value="$emit('update:modelValue', $event)"
  >

    <BaseCard>

      <slot />

    </BaseCard>

  </v-dialog>

</template>

<script setup>
import BaseCard from './BaseCard.vue'

defineProps({
  modelValue: Boolean
})

defineEmits(['update:modelValue'])
</script>
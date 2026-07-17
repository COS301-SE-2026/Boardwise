<template>
  <v-dialog
    :model-value="modelValue"
    :max-width="maxWidth"
    scrollable
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <slot />
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup>
defineProps({
  modelValue: Boolean,
  maxWidth: {
    type: [String, Number],
    default: 600
  }
})

defineEmits(['update:modelValue'])
</script>
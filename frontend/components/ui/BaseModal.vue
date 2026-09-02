<template>
  <v-dialog
    :model-value="modelValue"
    :max-width="maxWidth"
    scrollable
    :aria-labelledby="title ? titleId : undefined"
    :aria-label="title ? undefined : ariaLabel"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <v-card rounded="lg" class="base-modal
    ">
    <v-card-title
        v-if="title"
        :id="titleId"
        class="base-modal__header"
      >
      <span class="base-modal__title">
      {{ title  }}
      </span>

      <BaseButton
          v-if="closable"
          variant="text"
          icon="mdi-close"
          aria-label="Close dialog"
          @click="
            $emit('update:modelValue', false)
          "
        />
    </v-card-title>
      <v-card-text class="pa-6">
        <slot />
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { useId } from 'vue'
import BaseButton from './BaseButton.vue'

defineProps({
  modelValue: {
    type:Boolean,
    default: false  
  },
  title: {
    type: String,
    default: ''
  },
  ariaLabel: {
    type: String,
    default: 'Dialog'
  },
  maxWidth: {
    type: [String, Number],
    default: 600
  },
  closable: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue'])

const titleId = useId()
</script>
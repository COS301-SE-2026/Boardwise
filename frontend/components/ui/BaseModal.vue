<template>
  <v-dialog
    :model-value="modelValue"
    :max-width="maxWidth"
    :persistent="!closable || loading"
    scrollable
    :aria-labelledby="title ? titleId : undefined"
    :aria-label="title ? undefined : ariaLabel"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <v-card rounded="lg" class="base-modal">
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
          :disabled="loading"
          @click="
            $emit('update:modelValue', false)
          "
        />
      </v-card-title>

      <v-card-text class="pa-6">
        <div v-if="loading" class="base-modal__loading" data-test="modal-loading">
          <v-progress-circular indeterminate color="primary" />
        </div>
        <slot v-else />
      </v-card-text>

      <v-card-actions v-if="$slots.actions" class="base-modal__actions">
        <slot name="actions" />
      </v-card-actions>
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
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue'])

const titleId = useId()

if (import.meta.env?.DEV && !props.title && props.ariaLabel === 'Dialog') {
  console.warn('[BaseModal] Provide either a "title" or a specific "ariaLabel" — the default "Dialog" label isn\'t descriptive for screen reader users.')
}
</script>
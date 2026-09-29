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
        <BaseButton
          v-if="closable"
          variant="text"
          icon="mdi-close"
          aria-label="Close dialog"
          :disabled="loading"
          class="mr-2"
          @click="
            $emit('update:modelValue', false)
          "
        >
          <v-icon>mdi-close</v-icon>
        </BaseButton>

        <span class="base-modal__title">
          {{ title  }}
        </span>
      </v-card-title>

      <v-card-text class="pa-6">
        <BaseLoadingState v-if="loading" />
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
import BaseLoadingState from './BaseLoadingState.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true  
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
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const titleId = useId()

if (import.meta.env?.DEV && !props.title && props.ariaLabel === 'Dialog') {
  console.warn('[BaseModal] Provide either a "title" or a specific "ariaLabel" — the default "Dialog" label isn\'t descriptive for screen reader users.')
}
</script>
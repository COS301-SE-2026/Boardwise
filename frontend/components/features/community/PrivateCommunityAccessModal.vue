<template>
  <v-dialog
    :model-value="modelValue"
    max-width="480"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <v-card class="private-community-modal pa-6">
      <div class="private-community-modal__icon">
        <v-icon
          icon="mdi-lock-outline"
          size="40"
          color="primary"
          aria-hidden="true"
        />
      </div>

      <h2 class="private-community-modal__title">
        Private community
      </h2>

      <p class="private-community-modal__message">
        <strong>{{ community?.name }}</strong> is a private community.
        You need approval from the community owner before you can view
        its discussions, members and other private information.
      </p>

      <p
        v-if="requested"
        class="private-community-modal__success"
        role="status"
      >
        Your request has been sent to the community owner.
      </p>

      <div class="private-community-modal__actions">
        <BaseButton
          variant="secondary"
          :disabled="loading"
          @click="emit('update:modelValue', false)"
        >
          {{ requested ? 'Close' : 'Cancel' }}
        </BaseButton>

        <BaseButton
          v-if="!requested"
          :disabled="loading"
          @click="emit('request')"
        >
          {{ loading ? 'Sending request…' : 'Request to join' }}
        </BaseButton>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  community: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  },
  requested: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'request'
])
</script>
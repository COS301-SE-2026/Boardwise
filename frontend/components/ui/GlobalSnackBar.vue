<template>
  <v-snackbar
    v-model="visible"
    :timeout="4500"
    location="bottom right"
    rounded="xl"
    class="boardwise-snackbar"
    :class="`boardwise-snackbar--${color}`"
  >
    <div
      class="boardwise-snackbar__content"
      :role="color === 'error' ? 'alert' : 'status'"
    >
      <div class="boardwise-snackbar__icon">
        <v-icon
          :icon="response.icon"
          size="22"
        />
      </div>

      <div class="boardwise-snackbar__copy">
        <span class="boardwise-snackbar__title">
          {{ response.title }}
        </span>

        <span class="boardwise-snackbar__message">
          {{ message }}
        </span>
      </div>
    </div>

    <template #actions>
      <v-btn
        icon="mdi-close"
        variant="text"
        size="small"
        aria-label="Dismiss response"
        @click="visible = false"
      />
    </template>
  </v-snackbar>
</template>

<script setup>
import { computed } from 'vue'
import { useSnackBar } from '~/composables/useSnackbar'

const {
  visible,
  message,
  color
} = useSnackBar()

const response = computed(() => {
  const states = {
    success: {
      title: 'Nice move!',
      icon: 'mdi-check-circle-outline'
    },

    info: {
      title: 'Your move.',
      icon: 'mdi-information-outline'
    },

    warning: {
      title: 'Heads up.',
      icon: 'mdi-alert-outline'
    },

    error: {
      title: 'That move didn’t land.',
      icon: 'mdi-alert-circle-outline'
    }
  }

  return states[color.value] ?? states.info
})
</script>
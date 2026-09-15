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
      <BaseImage
        :src="response.mascotSrc"
        :alt="response.title"
        height="36px"
        width="36px"
        fit="contain"
        class="boardwise-snackbar__mascot"
      />

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
      <BaseButton
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
import BaseButton from './BaseButton.vue'
import BaseImage from './BaseImage.vue'

const {
  visible,
  message,
  color
} = useSnackBar()

const response = computed(() => {
  const states = {
    success: {
      title: 'Nice move!',
      icon: '/images/Boarley_cute.svg'
    },

    info: {
      title: 'Your move.',
      icon: '/images/BoarleySide.svg'
    },

    warning: {
      title: 'Heads up.',
      icon: '/images/BoarleySide.svg'
    },

    error: {
      title: 'That move didn’t land.',
      icon: '/images/BoarleySide.svg'
    }
  }

  return states[color.value] ?? states.info
})
</script>
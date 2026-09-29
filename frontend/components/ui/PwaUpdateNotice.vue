
<template>
  <section
    v-if="visible"
    class="pwa-update-notice"
    aria-label="Application update"
  >
    <output
      class="pwa-update-notice__message"
      aria-live="polite"
      aria-atomic="true"
    >
      <strong class="pwa-update-notice__title">
        {{ failed ? 'Update could not be applied' : 'A Boardwise update is ready' }}
      </strong>

      <span>
        {{
          failed
            ? 'Check your connection and try again.'
            : 'Save your work before reloading Boardwise.'
        }}
      </span>
    </output>

    <div class="pwa-update-notice__actions">
      <BaseButton
        variant="secondary"
        :disabled="updating"
        @click="dismissed = true"
      >
        Later
      </BaseButton>

      <BaseButton
        :loading="updating"
        :disabled="updating"
        @click="reloadApp"
      >
        {{ failed ? 'Try again' : 'Reload now' }}
      </BaseButton>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import BaseButton from './BaseButton.vue'

const { $pwa } = useNuxtApp()

const dismissed = ref(false)
const updating = ref(false)
const failed = ref(false)

const visible = computed(() =>
  Boolean($pwa?.needRefresh) && !dismissed.value
)

const reloadApp = async () => {
  if (!$pwa || updating.value) return

  updating.value = true
  failed.value = false

  try {
    await $pwa.updateServiceWorker(true)
  } catch {
    failed.value = true
  } finally {
    updating.value = false
  }
}
</script>

<template>
  <div class="pwa-connection-status">
    <output
      v-if="offline"
      class="pwa-offline"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
    >
      <v-icon
        icon="mdi-wifi-off"
        aria-hidden="true"
      />

      <span>
        <strong>You’re offline.</strong>
        Saved pages may remain available. Messages, uploads
        and changes need a connection.
      </span>

      <a href="/offline.html">
        Offline help
      </a>
    </output>

    <output
      v-else-if="showReconnected"
      class="pwa-reconnected"
      role="status"
      aria-live="polite"
      aria-atomic="true"
    >
      <v-icon
        icon="mdi-wifi-check"
        aria-hidden="true"
      />

      <span>
        <strong>Back online.</strong>
        Boardwise features are available again.
      </span>
    </output>
  </div>
</template>

<script setup lang="ts">
import {
  onMounted,
  onUnmounted,
  ref
} from 'vue'

const offline = ref(false)
const showReconnected = ref(false)

let reconnectTimer: ReturnType<typeof setTimeout> | undefined

const syncConnection = () => {
  if (!import.meta.client) return

  const wasOffline = offline.value
  offline.value = !navigator.onLine

  if (wasOffline && !offline.value) {
    showReconnected.value = true

    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }

    reconnectTimer = setTimeout(() => {
      showReconnected.value = false
    }, 5000)
  }

  if (offline.value) {
    showReconnected.value = false
  }
}

onMounted(() => {
  offline.value = !navigator.onLine

  window.addEventListener('online', syncConnection)
  window.addEventListener('offline', syncConnection)
})

onUnmounted(() => {
  window.removeEventListener('online', syncConnection)
  window.removeEventListener('offline', syncConnection)

  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})
</script>
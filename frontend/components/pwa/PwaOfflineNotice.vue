<template>
  <output 
    v-if="offline" 
    class="pwa-offline" 
    aria-live="polite"
    >
    You’re offline. Cached pages may open; chat, uploads and changes need a connection.
    <a href="/offline.html">Offline help</a>
  </output>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
const offline = ref(false)
const sync = () => { offline.value = !navigator.onLine }
onMounted(() => { sync(); window.addEventListener('online', sync); window.addEventListener('offline', sync) })
onUnmounted(() => { window.removeEventListener('online', sync); window.removeEventListener('offline', sync) })
</script>


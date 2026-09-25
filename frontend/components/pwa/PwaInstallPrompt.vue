<script setup lang="ts">
import { usePwaInstall } from '~/composables/usePwaInstall'
import { useRoute } from 'vue-router'
const { visible, ios, busy, error, install, dismiss } = usePwaInstall()
const route = useRoute()
</script>
<template>
  <aside v-if="visible && !route.path.startsWith('/auth')" class="pwa-install" aria-label="Install Boardwise">
    <div class="pwa-install__copy">
      <strong>Take Boardwise with you</strong>
      <p v-if="ios">In Safari, open Share, then choose Add to Home Screen.</p>
      <p v-else>Install Boardwise for quick access from your home screen.</p>
      <p v-if="error" role="alert">{{ error }}</p>
    </div>
    <div class="pwa-install__actions">
      <v-btn variant="text" @click="dismiss">Later</v-btn>
      <v-btn v-if="!ios" color="primary" :loading="busy" @click="install">Install</v-btn>
      <v-btn v-else color="primary" @click="dismiss">Got it</v-btn>
    </div>
  </aside>
</template>

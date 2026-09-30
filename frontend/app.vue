<script setup lang="ts">
import GlobalSnackBar from './components/ui/GlobalSnackBar.vue'
import MobileBottomNav from './components/layout/MobileBottomNav.vue'
import PwaInstallPrompt from './components/pwa/PwaInstallPrompt.vue'
import PwaOfflineNotice from './components/pwa/PwaOfflineNotice.vue'
import PwaUpdateNotice from './components/ui/PwaUpdateNotice.vue'
import PwaTaskStatus from './components/pwa/PwaTaskStatus.vue'

</script>

<template>
  <v-app>
    <NuxtPwaManifest />

    <PwaOfflineNotice />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>

    <PwaTaskStatus/>
    <MobileBottomNav />
    <PwaInstallPrompt />
    <GlobalSnackBar />
    <PwaUpdateNotice />
  </v-app>
</template>
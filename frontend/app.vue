<script setup lang="ts">
import MobileBottomNav from './components/layout/MobileBottomNav.vue';
import GlobalSnackBar from './components/ui/GlobalSnackBar.vue';
import PwaUpdateNotice from './components/ui/PwaUpdateNotice.vue';
</script>

<template>
  <v-app>
    <NuxtPwaManifest />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <MobileBottomNav/>
    <GlobalSnackBar />
    <PwaUpdateNotice/>
  </v-app>
</template>
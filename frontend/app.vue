<script setup lang="ts">
import GlobalSnackBar from './components/ui/GlobalSnackBar.vue';
import PwaUpdateNotice from './components/ui/PwaUpdateNotice.vue';
</script>

<template>
  <v-app>
    <NuxtPwaManifest />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <GlobalSnackBar />
    <PwaUpdateNotice/>
  </v-app>
</template>
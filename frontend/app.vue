<script setup lang="ts">
import GlobalSnackBar from './components/ui/GlobalSnackBar.vue';
import MobileBottomNav from './components/layout/MobileBottomNav.vue';

</script>

<template>
  <v-app>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <MobileBottomNav />
    <GlobalSnackBar />
  </v-app>
</template>
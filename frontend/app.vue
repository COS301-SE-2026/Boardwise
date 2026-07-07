<script setup lang="ts">
import GlobalSnackBar from './components/ui/GlobalSnackBar.vue';

</script>

<template>
  <v-app>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <GlobalSnackBar />
  </v-app>
</template>
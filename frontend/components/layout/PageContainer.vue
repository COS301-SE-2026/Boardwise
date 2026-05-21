<template>
  <v-main>
    <v-container fluid class="container">
      <slot />
    </v-container>
  </v-main>
</template>

<style scoped>
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
</style>
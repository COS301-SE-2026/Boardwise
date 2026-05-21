<template>
  <v-card
  class="add-card d-flex flex-column justify-center align-center"
  variant="outlined"
  @click="$emit('add-game')"
  hover
>
  <span class="text-h3 font-weight-bold">
    +
  </span>
  <p class="mt-3">
    Add Game
  </p>
</v-card>
</template>

<script setup>
defineEmits(['add-game'])
</script>

<style scoped>
.add-card {
  height: 280px;

  border: 2px dashed #ccc;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  cursor: pointer;
}
</style>
<template>
<v-card
  class="add-card d-flex flex-column justify-center align-center"
  height="320"
  variant="outlined"
  style="border: 2px dashed #ccc; cursor: pointer;"
  @click="$emit('click')"
>
  <span class="text-h3 font-weight-bold">+</span>
  <p class="mt-3 font-weight-semibold">Add Listing</p>
</v-card>
</template>

<script setup>

</script>
<template>
  <v-dialog v-model="open" max-width="400">
    <v-card class="pa-6 d-flex flex-column ga-5">

      <h2>Delete Listing</h2>

      <p>Are you sure you want to delete this listing?</p>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="open = false">Cancel</v-btn>
        <v-btn color="error" @click="open = false">Delete</v-btn>
      </div>

    </v-card>
  </v-dialog>
</template>

<script setup>
const open = defineModel()
</script>
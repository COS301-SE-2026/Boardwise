<template>
  <div> 
    <h1 class="text=h4 font-weight-bold mb-4">
      {{  rulebook.gameName }}
    </h1>

    <v-card class="pa-6">
      <p>This is where the rulebook content will go.</p>
      <p>You can later replace this with:</p>
      <ul>
        <li>PDF Viewer</li>
        <li>Markdown</li>
        <li>Rich text</li>
      </ul>
    </v-card>
  </div>
</template>

<script setup>
const props = defineProps({
  rulebook: Object
})
</script>
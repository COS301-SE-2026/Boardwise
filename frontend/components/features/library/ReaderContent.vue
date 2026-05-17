<template>
  <div class="reader">

    <h1>{{ rulebook.title }}</h1>

    <div class="page">
      <p>
        This is where the rulebook content will go.
      </p>

      <p>
        You can later replace this with:
      </p>

      <ul>
        <li>PDF Viewer</li>
        <li>Markdown</li>
        <li>Rich text</li>
      </ul>
    </div>

  </div>
</template>

<script setup>
const props = defineProps({
  rulebook: Object
})
</script>

<style scoped>
.reader {
  padding: 20px 0;
}

.page {
  margin-top: 20px;
  padding: 24px;
  background: white;
  border-radius: 12px;
}
</style>
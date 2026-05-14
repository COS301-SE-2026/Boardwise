<template>
  <div class="empty">
    <h3>{{ title }}</h3>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  message: String
})
</script>

<style scoped>
.empty {
  text-align: center;

  padding: 48px 24px;

  color: #777;
}
</style>
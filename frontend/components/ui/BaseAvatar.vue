<template>
  <img :src="src" class="avatar" />
</template>

<script setup>
defineProps({
  src: String
})
</script>

<style scoped>
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}
</style>
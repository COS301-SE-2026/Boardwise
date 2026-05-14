<template>
  <BaseCard class="card">

    <img
      :src="image"
      :alt="title"
    />

    <div class="content">

      <h3>{{ title }}</h3>

      <p>{{ category }}</p>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  title: String,
  category: String,
  image: String
})
</script>

<style scoped>
.card {
  overflow: hidden;
  padding: 0;
}

img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.content {
  padding: 16px;
}

h3 {
  margin: 0;
}

p {
  margin-top: 8px;
  color: #666;
}
</style>
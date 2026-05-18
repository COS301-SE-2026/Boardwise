<template>
  <NuxtLink
    :to="`/rulebook/${book.id}`"
    class="card-link"
  >

    <BaseCard class="card">

      <img
        :src="book.image"
        :alt="book.title"
      >

      <div class="content">

        <h3>{{ book.title }}</h3>

        <p>{{ book.category }}</p>

      </div>

    </BaseCard>

  </NuxtLink>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  book: Object
})
</script>

<style scoped>
.card-link {
  text-decoration: none;
  color: inherit;
}

.card {
  width: 220px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
}

img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.content {
  padding: 16px;
}

h3 {
  margin: 0;
}

p {
  color: #777;
  margin-top: 8px;
}
</style>
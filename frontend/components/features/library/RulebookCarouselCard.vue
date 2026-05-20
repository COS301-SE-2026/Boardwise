<template>
  <NuxtLink
    :to="`/library/${rulebook.id}`"
    class="card-link"
  >

    <BaseCard class="card">

      <img
        :src="rulebook.image"
        :alt="rulebook.title"
      >

      <div class="content">

        <h3>{{ rulebook.title }}</h3>

        <p>{{ rulebook.category }}</p>

      </div>

    </BaseCard>

  </NuxtLink>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  rulebook: Object
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
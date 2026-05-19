<template>
  <ReaderLayout v-if="rulebook" :rulebook="rulebook" />

  <div v-else class="not-found">
    <p>Rulebook not found.</p>
    <NuxtLink to="/library">← Back to Library</NuxtLink>
  </div>
</template>

<script setup>
import { rulebooks } from '~/services/mockData/rulebooks.js'
import ReaderLayout from '~/components/features/library/ReaderLayout.vue'

const route = useRoute()

const rulebook = computed(() =>
  rulebooks.find(item => item.id === Number(route.params.id))
)
</script>

<style scoped>
.not-found {
  text-align: center;
  margin-top: 80px;
  color: #666;
}

.not-found a {
  color: #6C3BFF;
  text-decoration: none;
  display: block;
  margin-top: 12px;
}
</style>
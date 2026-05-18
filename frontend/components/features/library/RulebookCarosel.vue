<template>
  <section class="carousel-section">

    <div class="top">
      <SectionTitle :title="title" />
    </div>

    <div class="carousel">

      <RulebookCarouselCard
        v-for="book in books"
        :key="book.id"
        :book="book"
      />

    </div>

  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import RulebookCarouselCard from './RulebookCarouselCard.vue'

defineProps({
  title: String,
  books: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.carousel-section {
  margin-top: 40px;
}

.carousel {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 12px;
}

.carousel::-webkit-scrollbar {
  height: 8px;
}

.carousel::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 999px;
}
</style>
<template>
  <div class="mt-10">
    <SectionTitle :title="title" />
    <div class="d-flex ga-5 overflow-x-auto pb-3 mt-4">
      <RulebookCarouselCard
        v-for="book in rulebooks"
        :key="book.id"
        :rulebook="book"
      />
    </div>
  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import RulebookCarouselCard from './RulebookCarouselCard.vue'

defineProps({
  title: String,
  rulebooks: {
    type: Array,
    default: () => []
  }
})
</script>
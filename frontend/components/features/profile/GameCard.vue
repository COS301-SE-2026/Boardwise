<template>
  <BaseCard class="game-card">

    <v-img :src="image" :alt="title" height="180" cover />

    <v-card-text>
      <h3 class="game-card__title ma-0">{{ title }}</h3>
      <p class="game-card__category ma-0 mt-1">{{ category }}</p>
    </v-card-text>

    <!-- <RulebookDetail
      v-model="showDetail"
      :game="{ title, category, image }"
    /> -->

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
// import RulebookDetail from '~/components/features/library/RulebookDetail.vue'

defineProps({
  title: String,
  genres: String,
  imageUrl: String,
})

const showDetail = ref(false)
</script>

<style scoped>

.game-card {
  /* cursor:     pointer; */
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}
.game-card:hover {
  transform:  translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.game-card__title {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  font-weight: var(--fw-regular);
  color: var(--color-secondary);
}

.game-card__category {
  font-family: var(--font-body);
  font-size:   var(--fs-small);
  color: var(--color-text-muted);
}
</style>
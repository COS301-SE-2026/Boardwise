<template>
  <div class="mt-10">
    <SectionTitle title="Popular Rulebooks" />

    <v-skeleton-loader 
      v-if="!rulebooks.length"
      class="mt-4"
      type="image"
      height="320"
    />

    <v-carousel 
      v-else
      height="320"
      hide-delimiter-background
      show-arrows="hover"
      class="popular-carousel mt-4"
      cycle
    >
      <v-carousel-item
        v-for="rulebook in rulebooks"
        :key="rulebook.id"
      >
        <v-container v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 60vh">
          <v-progress-circular indeterminate color="primary" size="48" />
        </v-container>

        <v-carousel-item
          v-else
          v-for="rulebook in rulebooks"
          :key="rulebook.id"
        >
          <BaseImage 
            :src="rulebook.coverUrl" 
            :alt="rulebook.title" 
            height="100%" 
            fit="cover" 
          />

          <div class="carousel-caption">
            <div class="carousel-content">
              <v-chip
                color="primary"
                size="small"
                variant="flat"
                class="mb-3"
              >
                Popular
              </v-chip>

              <h2>{{  rulebook.title }}</h2>

              <p>{{ rulebook.genre }}</p>
              
              <v-btn 
                color="primary"
                rounded="pill"
              >
                Read Rulebook
              </v-btn>
            </div>
          </div>
        </v-carousel-item>
      </v-carousel-item>
    </v-carousel>
  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import { useLibrary } from '~/composables/useLibrary';

const { isLoading } = useLibrary()

defineProps({
  title: String,
  rulebooks: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select'])

</script>

<style scoped>
.popular-carousel {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}
.carousel-slide {
  position: relative;
  height: 100%;
  cursor: pointer;
}

.carousel-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;

  padding: 32px;

  background: linear-gradient(
    to top,
    rgba(0,0,0,0.8),
    rgba(0,0,0,0.35)
    transparent
  );
}

.carousel-content {
  max-width: 420px;
  color: white;
}

.carousel-content h2 {
  margin-bottom: .5rem;
  font-size: 2rem;
  font-weight: 700;
}

.carousel-content p {
  opacity: .9;
  margin-bottom: 1rem;
}

:deep(.v-carousel__controls) {
  bottom: 12px;
}

:deep(.v-carousel__controls__item) {
  color: white;
}

:deep(.v-window__prev),
:deep(.v-window__next) {
  background: rgba(255,255,255,.15);
  backdrop-filter: blur(8px);
  border-radius: 50%;
}
</style>
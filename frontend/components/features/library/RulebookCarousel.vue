<template>
  <div class="mt-10">
    <SectionTitle title="Popular Rulebooks" />

    <div class="position-relative mt-4">
      <v-carousel
        height="320"
        hide-delimiter-background
        show-arrows="hover"
        class="mt-4 popular-carousel"
        cycle
      >
        <v-container v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 60vh">
          <v-progress-circular indeterminate color="primary" size="48" />
        </v-container>

        <v-carousel-item
          v-else
          v-for="rulebook in rulebooks"
          :key="rulebook.id"
        >
          <div class="carousel-slide" @click="$emit('select', rulebook)">
            <BaseImage 
              :src="rulebook.coverUrl" 
              :alt="rulebook.title" 
              height="320px" 
              fit="cover" 
            />

            <div class="carousel-caption">
              <p class="text-h6 font-weight-bold text-white mb-0">
                {{ rulebook.title }}
              </p>
            </div>
          </div>
        </v-carousel-item>
      </v-carousel>
    </div>
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
.carousel-slide {
  position: relative;
  height: 100%;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
}

.carousel-caption {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 24px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.65), transparent);
}
</style>
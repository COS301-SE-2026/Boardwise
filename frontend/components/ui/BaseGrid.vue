<template>
  <div class="base-grid">
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  cols: {
    type: String,
    default: '280px'
  },
  gap: {
    type: String,
    default: '20px'
  }
})

const gridColumns = computed(() => `repeat(auto-fill, minmax(${props.cols}, 300px))`)
</script>

<style scoped>
.base-grid {
  display: grid;
  grid-template-columns: v-bind(gridColumns);
  gap: v-bind(gap);
  align-items: stretch;
  width: 100%;
}

@media (max-width: 900px) {
  .base-grid {
    grid-template-columns: repeat(2, 300px);
  }
}

@media (max-width: 600px) {
  .base-grid {
    grid-template-columns: 1fr;
  }
}
</style>
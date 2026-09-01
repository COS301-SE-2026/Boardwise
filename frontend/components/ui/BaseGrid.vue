<template>
  <div class="base-grid" :style="gridVariables">
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

const gridVariables = computed(() => ({
  '--base-grid-min': props.cols,
  '--base-grid-gap': props.gap
}))
const gridColumns = computed(() => `repeat(auto-fill, minmax(${props.cols}, 300px))`)
</script>

<style scoped>
.base-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: v-bind(gap);
  align-items: stretch;
  width: 100%;
}
</style>
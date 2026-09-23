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
</script>
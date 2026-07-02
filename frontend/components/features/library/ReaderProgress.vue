<template>
  <div class="d-flex align-center gap-4 px-10 py-3 bg-white border-b">
    <v-progress-linear
      :model-value="percentage"
      color="primary"
      rounded
      height="6"
      style="flex: 1;"
    />
    
    <span class="text-caption text-medium-emphasis text-no-wrap">
      {{ percentage }}% complete
    </span>
  </div>
</template>

<script setup>
const props = defineProps({
  currentPage: Number,
  totalPages: Number
})

const percentage = computed(() =>
  props.totalPages > 0
    ? Math.round(((props.currentPage + 1) / props.totalPages) * 100)
    : 0
)
</script>
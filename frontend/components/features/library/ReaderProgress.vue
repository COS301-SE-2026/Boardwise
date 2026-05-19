<template>
  <div class="progress-bar">
    <div class="track">
      <div class="fill" :style="{ width: `${percentage}%` }" />
    </div>
    <span class="label">{{ percentage }}% complete</span>
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

<style scoped>
.progress-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 40px;
  background: white;
  border-bottom: 1px solid #eee;
}

.track {
  flex: 1;
  height: 6px;
  background: #eee;
  border-radius: 999px;
  overflow: hidden;
}

.fill {
  height: 100%;
  background: #6C3BFF;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.label {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .progress-bar { padding: 10px 16px; }
}
</style>
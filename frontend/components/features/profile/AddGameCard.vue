<template>
  <BaseCard
    class="add-card d-flex flex-column justify-center align-center"
    @click="$emit('add-game')"
  >
    <span style="font-size: var(--fs-h1); font-weight: var(--fw-bold);">+</span>
    <p style="margin-top: var(--space-3); margin-bottom: 0;">Add Game</p>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineEmits(['add-game'])
</script>

<style scoped>
.add-card {
  cursor:     pointer;
  overflow:   hidden;

  height:     280px;
  width: 100%;

  border:     2px dashed #ccc;
  border-radius: 16px;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.add-card:hover {
  transform:        translateY(-2px);
  background-color: var(--color-surface-alt);
  box-shadow:       var(--shadow-md) !important;
}
</style>
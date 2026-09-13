<template>
  <BaseCard
    class="add-card d-flex flex-column justify-center align-center"
  >
    <span class="text-h3 font-weight-bold">+</span>
    <p class="mt-3">Add Listing</p>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
</script>

<style scoped>
.add-card {
  cursor:     pointer;
  overflow:   hidden;
  height:     300px;
  border:     2px dashed #ccc;
  margin-top: 25px;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.add-card:hover {
  transform:        translateY(-2px);
  background-color: var(--color-surface-alt);
  box-shadow:       var(--shadow-md) !important;
}
</style>
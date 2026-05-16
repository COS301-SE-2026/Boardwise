<template>
  <BaseCard class="add-card">

    <div class="plus">
      +
    </div>

    <p>Add Game</p>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineEmits(['click'])
</script>

<style scoped>
.add-card {
  height: 280px;

  border: 2px dashed #ccc;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  cursor: pointer;
}

.plus {
  font-size: 48px;
  font-weight: bold;
}

p {
  margin-top: 12px;
}
</style>
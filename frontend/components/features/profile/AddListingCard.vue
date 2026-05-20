<template>
  <BaseCard 
    class="add-card"
    @click="$emit('click')"
  >

    <div class="plus">
      +
    </div>

    <p>Add Listing</p>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
</script>

<style scoped>
.add-card {
  height: 320px;

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
  font-weight: 600;
}
</style>
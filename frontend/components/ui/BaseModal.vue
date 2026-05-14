<template>
  <div v-if="visible" class="overlay">

    <BaseCard class="modal">

      <slot />

    </BaseCard>

  </div>
</template>

<script setup>
import BaseCard from './BaseCard.vue'

defineProps({
  visible: Boolean
})
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;

  background: rgba(0,0,0,0.5);

  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  width: 90%;
  max-width: 700px;
}
</style>
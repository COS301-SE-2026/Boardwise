<template>
  <div 
    v-if="modelValue" class="overlay"
    @click.self="$emit('update:modelValue', false)"
  >

    <BaseCard class="modal">

      <slot />

    </BaseCard>

  </div>
</template>

<script setup>
import BaseCard from './BaseCard.vue'

defineProps({
  modelValue: Boolean
})

defineEmits(['update:modalValue'])
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal {
  width: 90%;
  max-width: 1200px;
  max-height: 150vh;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
}
</style>
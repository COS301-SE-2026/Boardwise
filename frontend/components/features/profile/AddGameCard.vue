<template>
  <base-card
    class="add-card d-flex flex-column justify-center align-center"
    @click="showAdd = true"
  >
    <span class="text-h3 font-weight-bold">+</span>
    <p class="mt-3">Add Game</p>

  </base-card>
</template>

<script setup>
import AddGameModal from '~/components/features/profile/AddGameModal.vue'

const emit = defineEmits(['game-added'])
const showAdd = ref(false)

const handleConfirm = (game) => {
  emit('game-added', {
    id:       Date.now(),
    title:    game.title,
    category: game.category,
    image:    '/placeholder.png',
  })
}
</script>

<style scoped>
.add-card {
  cursor:     pointer;
  overflow:   hidden;
  height:     280px;
  border:     2px dashed #ccc;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.add-card:hover {
  transform:        translateY(-2px);
  background-color: var(--color-surface-alt);
  box-shadow:       var(--shadow-md) !important;
}
</style>
<template>
  <BaseAddCard
    label="Add Game"
    @click="$emit('add-game')"
  />
</template>

<script setup>
import BaseAddCard from '~/components/ui/BaseAddCard.vue';

defineEmits(['add-game'])
</script>
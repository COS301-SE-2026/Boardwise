<template>
  <BaseAddCard
   label="Add Listing"
   @click="$emit('add-listing')"
  />
</template>

<script setup>
import BaseAddCard from '~/components/ui/BaseAddCard.vue'

defineEmits(['add-listing'])
</script>
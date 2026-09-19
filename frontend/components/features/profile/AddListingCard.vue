<template>
  <BaseAddCard 
    label="Add Listing"
    @click="showAdd = true"
  />
</template>

<script setup>
import BaseAddCard from '~/components/ui/BaseAddCard.vue'

import { ref } from 'vue'

const showAdd = ref(false)
</script>
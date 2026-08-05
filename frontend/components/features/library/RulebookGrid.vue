<template> 
    <v-row class="mt-8">
    <v-col
      v-for="rulebook in rulebooks"
      :key="rulebook.id"
      cols="6" sm="4" md="3" lg="2"
    >
      <RulebookCard
        :rulebook="rulebook"
        @click="$emit('select', $event)"
      />
    </v-col>
  </v-row>
</template>

<script setup>
import RulebookCard from './RulebookCard.vue'

defineProps({
  rulebooks: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select'])
</script>
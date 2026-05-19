<template> 
    <div class="grid">

        <RulebookCard 
            v-for="rulebook in rulebooks"
            :key="rulebook.id"
            :rulebook="rulebook"
            @click="$emit('select', rulebook)"
        />
    </div>
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

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
  margin-top: 32px;
}
</style>
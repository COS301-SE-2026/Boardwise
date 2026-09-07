<template> 
  <div class="mt-8">
    <v-row 
      v-if="gameView === 'grid'"
      class="rulebook-grid"
      >
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

  <div  
    v-else
    class="rulebook-list"
    >
      <RulebookListItem
        v-for="rulebook in rulebooks"
        :key="rulebook.id"
        :rulebook="rulebook"
        @click="$emit('select', $event)"
      />
  </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import RulebookCard from './RulebookCard.vue'
import RulebookListItem from './RulebookListItem.vue'
import { useAppearancePreferences } from '~/composables/useAppearancePreferences.ts';
defineProps({
  rulebooks: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select'])

const {
  preferences,
  loadPreferences
} = useAppearancePreferences()

const gameView = computed(() => preferences.value.gameView)

onMounted(() => {
  loadPreferences(false)
})
</script>
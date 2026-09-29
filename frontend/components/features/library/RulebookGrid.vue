<template> 
  <div class="mt-8">
    <BaseGrid v-if="gameView === 'grid'">
      <RulebookCard
        v-for="rulebook in rulebooks"
        :key="rulebook.id"
        :rulebook="rulebook"
        size="lg"
        @click="$emit('select', $event)"
      />
    </BaseGrid>

    <div v-else class="rulebook-list">
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

import BaseGrid from '~/components/ui/BaseGrid.vue';

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
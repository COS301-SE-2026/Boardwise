<template>
    <div class="onboarding-step">
        <OnboardingProgress :current="3" :total="4" />
        <BoarleyBubble>
            Which games do you own or play often? Search or filter by genres below.
        </BoarleyBubble>

        <p class="onboarding-step_hint">
            Select any you own - {{  minRequired  }} is just a suggestion, not a requirement
            <span v-if="selected.length">({{ selected.length }} selected)</span>
        </p>

        <BaseSearch 
            v-model="searchQuery"
            placeholder="Search for a game..."
            class="onboarding-search"
        />

        <div v-if="genreTabs.length > 1" class="onboarding-genre-tabs" role="tablist">
            <button
                v-for="tab in genreTabs"
                :key="tab.id"
                type="button"
                class="onboarding-genre-tab"
                :class="{ 'onboarding-genre-tab--active': activeTab === tab.id }"
                role="tab"
                :aria-selected="activeTab === tab.id"
                @click="activeTab = tab.id"
            >
                {{ tab.label }}
            </button>
        </div>

        <BaseGrid :columns="4" class="onboarding-class_game-grid">
            <v-chip
                v-for="game in filteredGames"
                :key="game.id"
                :color="selected.includes(game.id) ? 'primary' : undefined"
                :variant="selected.includes(game.id) ? 'elevated' : 'outlined'"
                class="base-tag"
                @click="toggleGame(game.id)"
            >
                {{  game.title }}
            </v-chip>
            
            <p v-if="filteredGames.length === 0" class="onboarding-step_hint">
                No games match "{{ searchQuery }}" — try a different search or genre.
            </p>
        </BaseGrid>

        <div class="onboarding-step_actions">
            <BaseButton
                variant="secondary"
                class="onboarding-step_skip"
                @click="$emit('skip')"
            >
                Skip for now
            </BaseButton>

            <BaseButton
                variant="primary"
                :disabled="selected.length < minRequired"
                class="onboarding-step_cta"
                @click="$emit('continue', selected)"
            >
                Continue
            </BaseButton>
        </div>
    </div>
</template>

<script setup>
import BoarleyBubble from './BoarleyBubble.vue'
import OnboardingProgress from './OnboardingProgress.vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

import { ref, computed } from 'vue'

const props = defineProps({
    games: { type: Array, required: true }, 
    selectedGenres: { type: Array, default: () => [] },
    minRequired: {type: Number, default: 5 }
})

defineEmits(['continue', 'skip'])

const selected = ref([])
const searchQuery = ref('')
const activeTab = ref('all')

const genreTabs = computed(() => [
    { id: 'all', label: 'All Games' },
    ...props.selectedGenres.map(g => ({ id: g.id, label: g.label }))
])

const filteredGames = computed(() => {
    let result = props.games 

    if(activeTab.value !== 'all') {
        result = result.filter(g => g.genres?.includes(activeTab.value))
    }

    if(searchQuery.value.trim()) {
        const q = searchQuery.value.trim().toLowerCase()
        result = result.filter(g => g.title.toLowerCase().includes(q))
    }
    
    return result
})

function toggleGame(id) {
    const i = selected.value.indexOf(id)
    if(i === -1) {
        selected.value.push(id)
    } else {
        selected.value.splice(i, 1)
    }
}
</script>
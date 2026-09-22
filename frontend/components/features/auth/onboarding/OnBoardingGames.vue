<template>
    <div class="onboarding-step onboarding-games">

        <div class="onboarding-games__topbar">
            <div class="onboarding-games__nav">
                <button type="button" class="onboarding-games__back" @click="$emit('back')">
                    <v-icon size="16">mdi-arror-left</v-icon>
                    Back to Genres
                </button>

                <button type="button" class="onboarding-games__skip-link" @click="$emit('skip')">
                    Skip for now
                </button>
            </div>

            <OnboardingProgress :current="3" :total="4" />
        </div>

        <span class="onboarding-eyebrow">
            <v-icon size="14">mdi-book-search-outline</v-icon>
            Boarley Knowledge Sync
        </span>

        <h1 class="onboarding-heading">Which games do you own or play often?</h1>
        
        <p class="onboarding-step_hint">
            We'll preload their official verified rulebooks and interactive Setup Wizards.
        </p>

        <BaseSearch 
            v-model="searchQuery"
            placeholder="Search tabletop games, expansions, or designers..."
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

        <div class="onboarding-games_body">
            <BaseGrid :columns="4" gap="16px" class="onboarding-class_game-grid">
                <BaseCard 
                    v-for="game in filteredGames"
                    :key="game.id"
                    class="onboarding-game-card"
                >
                    <template #media>
                        <BaseImage 
                            :src="game.imageUrl"
                            :alt="game.title"
                            height="140px"
                            fit="cover"
                        />
                    </template>

                    <span
                        v-if="game.wizardReady"
                        class="onboarding-game-card__badge onboarding-game-card__badge--wizard"
                    >
                        <v-icon size="12">mdi-auto-fix</v-icon>
                        Wizard Ready
                    </span>

                    <h3 class="onboarding-game-card__title">{{ game.title }}</h3>
                    <p v-if="game.description" class="onboarding-game-card__desc">
                        {{  game.description }}
                    </p>

                    <template #actions>
                        <button 
                            type="button"
                            class="onboarding-game-card__add"
                            :class="{ 'onboarding-game-card__add--selected': selected.includes(game.id) }"
                            @click="toggleGame(game.id)"
                        >
                            <v-icon size="16">
                                {{ selected.includes(game.id)  ? 'mdi-check-circle' : 'mdi-plus' }}
                            </v-icon>
                            {{ selected.includes(game.id) ? 'Added to Library' : 'Add' }}
                        </button>
                    </template>
                </BaseCard>
            </BaseGrid>

            <p v-if="filteredGames.length === 0" class="onboarding-step_hint">
                No games match "{{  searchQuery }}" - try a different search or genre.
            </p>
        </div>

    <div class="onboarding-games__footer">
        <div class="onboarding-game__footer-status">
            <v-icon size="20" color="primary">mdi-check-circle</v-icon>

            <div>
                <p class="onboarding-games__footer-title">
                    {{  selected.length }} game {{ selected.length === 1 ? '' : 's' }} ready with interactive Setup Wizards
                </p>

                <p class="onboarding-games__footer-subtitle">
                    Pre-loaded with verified card positioning and quick-start tokens
                </p>
            </div>
        </div>

        <BaseButton
            variant="primary"
            :loading="isSubmitting"
            :disabled="selected.length < minRequired || isSubmitting"
            class="onboarding-step_cta"
            @click="$emit('continue', selected)"
        >
            Next: Confirm Library ({{ selected.length }} Games)
            <v-icon size="16" end>mdi-arrow-right</v-icon>
        </BaseButton>
    </div>
</div>
</template>

<script setup>
import OnboardingProgress from './OnboardingProgress.vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

import { ref, computed } from 'vue'

const props = defineProps({
    games: { type: Array, required: true }, 
    selectedGenres: { type: Array, default: () => [] },
    minRequired: {type: Number, default: 5 },
    isSubmitting: { type: Boolean, default: false },
})

defineEmits(['continue', 'skip', 'back'])

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
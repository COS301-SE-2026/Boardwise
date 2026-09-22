<template>
    <section class="onboarding-card onboarding-genres">
        <OnboardingProgress :current="2" :total="4" label="Preferences" />

        <span class="onboarding-eyebrow">Tabletop Persona</span>

        <h1 class="onboarding-heading">What kinds of games hit your table?</h1>


        <p class="onboarding-step_hint">
            Select at least {{  minRequired }} genre {{  minRequired === 1 ? '' : 's' }}
            
            <span v-if="selected.length">
                ({{ selected.length }} selected)
            </span>
        </p>
        
        <div class="onboarding-genres__body">
            <BaseGrid :columns="4" class="onboarding-genre-grid">
                <button
                    v-for="genre in genres"
                    :key="genre.id"
                    type="button"
                    class="onboarding-genre-tile"
                    :class="{ 'onboarding-genre-tile--selected': selected.includes(genre.id) }"
                    :aria-pressed="selected.includes(genre.id)"
                    @click="toggleGenre(genre.id)"
                >
                    <div class="onboarding-genre-tile__icon-wrap">
                        <v-icon size="18">mdi-dice-multiple-outline</v-icon>
                    </div>

                    <v-icon 
                        v-if="selected.includes(genre.id)"
                        size="16"
                        class="onboarding-genre-tile__check"
                    >
                        mdi-check-circle
                    </v-icon>

                    <v-icon v-if="genre.icon" size="20" class="onboarding-genre-title__icon">
                        {{ genre.icon }}
                    </v-icon>
    
                    <span class="onboarding-genre-tile__label">{{ displayLabel(genre.label) }}</span>
    
                    <span v-if="genre.description" class="onboarding-genre-tile__desc">
                        {{ genre.description }}
                    </span>
                </button>
            </BaseGrid>

            <div class="onboarding-tip-card">
                <span class="onboarding-tip-card__eyebrow">
                    <v-icon size="14">mdi-lightbulb-outline</v-icon>
                    Boarley's Tip
                </span>

                <p class="onboarding-tip-card__text">
                    Pick what you actually enjoy - Boarley uses this to fast-track setup wizards for your favourites.
                </p>
            </div>
        </div>

        <div class="onboarding-ai-banner">
            <v-icon size="18" class="onboarding-ai-banner__icon">mdi-robot-outline</v-icon>
            <div>
                <span class="onboarding-ai-banner__title">Adaptive Tabletop AI Active</span>
                <p class="onboarding-ai-banner__desc">
                    Boarley loads card schemes, meeple counts, and setup checklists tailored to your playstyle.
                </p>
            </div>
        </div>

        <div class="onboarding-genres__footer">
            <div class="onboarding-genres__footer-left">
                <span class="onboarding-step_hint">
                    {{  selected.length  }} genre {{  selected.length === 1 ? '' : 's' }} selected
                </span>
                <span v-if="selected.length >= minRequired" class="onboarding-genres__goal-met">
                    <v-icon size="14">mdi-check-circle</v-icon>
                    Goal met
                </span>
                <button type="button" class="onboarding-genres__select-all" @click="selectAll">
                    I play everything (select all)
                </button>
            </div>
            
            <BaseButton
                variant="primary"
                :disabled="selected.length < minRequired"
                append-icon="mdi-arrow-right"
                class="onboarding-step_cta"
                @click="$emit('continue', selected)"
            >
                Continue to Game Selection ({{  selected.length }} selected)
            </BaseButton>
        </div>
    </section>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import OnboardingProgress from './OnboardingProgress.vue'

import { ref } from 'vue'

const props = defineProps({
    genres: { type: Array, required: true },
    minRequired: { type: Number, default: 2 }
})

defineEmits(['continue', 'skip'])

const selected = ref([])

function toggleGenre(id) {
    const i = selected.value.indexOf(id)
    if(i === -1) {
        selected.value.push(id)
    } else {
        selected.value.splice( i , 1)
    }
}

function selectAll() {
    selected.value = props.genres.map(g => g.id)
}

function displayLabel(label) {
    return label
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ')
}
</script>

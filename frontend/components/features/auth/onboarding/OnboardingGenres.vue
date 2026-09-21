<template>
    <div class="onboarding-step">
        <OnboardingProgress :current="2" :total="4" />

        <BoarleyBubble>
            What kinds of games hit your table? 
        </BoarleyBubble>

        <p class="onboarding-step_hint">
            Select at least {{  minRequired }} genre {{  minRequired === 1 ? '' : 's' }}
            
            <span v-if="selected.length">
                ({{ selected.length }} selected)
            </span>
        </p>

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
                <v-icon v-if="genre.icon" size="20" class="onboarding-genre-tile__icon">
                    {{ genre.icon }}
                </v-icon>
 
                <span class="onboarding-genre-tile__label">{{ genre.label }}</span>
 
                <span v-if="genre.description" class="onboarding-genre-tile__desc">
                    {{ genre.description }}
                </span>
 
                <v-icon
                    v-if="selected.includes(genre.id)"
                    size="16"
                    class="onboarding-genre-tile__check"
                >
                    mdi-check-circle
                </v-icon>
            </button>
        </BaseGrid>
    </div>

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
</template>

<script setup>
import BoarleyBubble from './BoarleyBubble.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import OnboardingProgress from '~/OnboardingProgress.vue'

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
</script>

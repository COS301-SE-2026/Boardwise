<template>
    <div class="onboarding-step">
        <BoarleyBubble>
            First, please select the games you own!
        </BoarleyBubble>

        <p class="onboarding-step_hint">
            Please select a minimum of {{  minRequired  }}
            <span v-if="selected.length">({{ selected.length }} selected)</span>
        </p>

        <BaseGrid :columns="4" class="onboarding-class_game-grid">
            <BaseTag
                v-for="game in games"
                :key="game.id"
                :selected="selected.includes(game.id)"
                clickable
                @click="toggleGame(game.id)"
            >
                {{  game.title }}
            </BaseTag>
            <!-- <div v-for="game in games" :key="game.id">{{  game.title }}</div> -->
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
import BoarleyBubble from './BoarleyBubble.vue';
import BaseGrid from '~/components/ui/BaseGrid.vue';
import BaseTag from '~/components/ui/BaseTag.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

import { ref } from 'vue'

const props = defineProps({
    games: { type: Array, required: true }, 
    minRequired: {type: Number, default: 5 }
})

defineEmits(['continue', 'skip'])

const selected = ref([])

function toggleGame(id) {
    const i = selected.value.indexOf(id)
    if(i === -1) 
    {
        selected.value.push(id)
    } else 
    {
        selected.value.splice(i, 1)
    }
}
</script>

<style scoped> 
.onboarding-step {
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-6);
    max-width: 720px;
    width: 100%;
}

.onboarding-step_hint {
    font-size: var(--fs-small);
    color: var(--color-text-muted);
}

.onboarding-class_game-grid {
    max-height: 320px;
    overflow-y: auto;
}

.onboarding-step_actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-3);
    margin-top: var(--space-4);
}

.onboarding-step_skip {
    color: var(--color-text-muted);
    font-size: var(--fs-small);
}

</style>
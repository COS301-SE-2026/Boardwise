<template>
    <BaseCard class="onboarding-step">
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
        </BaseGrid>

        <BaseButton
            variant="primary"
            :disabled="selected.length < minRequired"
            class="onboarding-step_cta"
            @click="$emit('continue', selected)"
        >
            Continue
        </BaseButton> 
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';
import BoarleyBubble from './BoarleyBubble.vue';
import BaseGrid from '~/components/ui/BaseGrid.vue';
import BaseTag from '~/components/ui/BaseTag.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

import { ref } from 'vue'

const props = defineProps({
    games: { type: Array, required: true }, 
    minRequired: {type: Number, default: 5 }
})

defineEmits(['continue'])

const selected = ref([])

function toggleGame(id) {
    const i = selected.value.indexOf(id)
    if(i === 1) 
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
    gap: var(--space-4);
    max-width: 480px;
}

.onboarding-step_hint {
    font-size: var(--fs-small);
    color: var(--color-text-muted);
}

.onboarding-step_game-grid {
    max-height: 320px;
    overflow-y: auto;
}

</style>
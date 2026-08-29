<template>
    <BaseCard class="onboarding-setup">
        <img src="/images/main.svg" alt="Boardwise"  class="onboarding-step_logo" />
        <h1 class="onboardoing-step_title">Welcome {{  username  }} to Boardwise!</h1>

        <BoarleyBubble>
            My name is Boarley. I'll helping you start your journey through Boardwise!
        </BoarleyBubble>

        <BaseButton variant="primary" class="onboarding-step_cta" @click="$emit('continue')">
            Continue
        </BaseButton>
    </BaseCard>
</template>

<script setup>
import BoarleyBubble from './BoarleyBubble.vue';
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseCard from '~/components/ui/BaseCard.vue';

defineProps({
    username: { 
        type: String, 
        required: true
    }
})

defineEmits(['continue'])
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

.onboarding-step_logo {
    width: clamp(px 30vw, 220px);
}
.onboarding-step_cta {
    align-self: center;
}
</style>
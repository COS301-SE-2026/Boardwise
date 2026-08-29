<template>
    <div class="onboarding-setup">
        <img src="/images/main.svg" alt="Boardwise"  class="onboarding-step_logo" />
        <h1 class="onboardoing-step_title">Welcome {{  username  }} to Boardwise!</h1>

        <BoarleyBubble>
            My name is Boarley. I'll helping you start your journey through Boardwise!
        </BoarleyBubble>

        <BaseButton variant="primary" class="onboarding-step_cta" @click="$emit('continue')">
            Continue
        </BaseButton>
    </div>
</template>

<script setup>
import BoarleyBubble from './BoarleyBubble.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

defineProps({
    username: { 
        type: String, 
        required: true
    }
})

defineEmits(['continue'])
</script>

<style scoped>
.onboarding-setup {
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-4);
    width: 100%;
    max-width: 640px;
}

.onboarding-step_logo {
    width: clamp(160px, 30vw, 220px);
}
.onboarding-step_cta {
    align-self: center;
}
</style>
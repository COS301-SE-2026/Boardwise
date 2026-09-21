<template>
    <div class="onboarding-setup">
        <OnboardingProgress :current="1" :total="4" />

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
import BoarleyBubble from './BoarleyBubble.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import OnboardingProgress from './OnboardingProgress.vue'

defineProps({
    username: { 
        type: String, 
        required: true
    }
})

defineEmits(['continue'])
</script>
<template>
    <section class="onboarding-card onboarding-welcome" aria-labelledby="onboarding-welcome-title">
        <OnboardingProgress :current="1" :total="4" label="Welcome Quest" />

        <div class="onboarding-welcome__avatar">
            <BaseAvatar src="/images/Boarley.svg" alt="Boarley" size="xl" />
            <span class="onboarding-welcome__role">Copilot</span>
        </div>

        <h1 id="onboardoing-welcome-title" class="onboarding-heading" tabindex="-1">
            Welcome {{  username  }} to Boardwise!
        </h1>

        <BoarleyBubble :show-avatar="false">
            My name is Boarley. I'll helping you start your journey through Boardwise!
        </BoarleyBubble>

        <div class="onboarding-welcome__features">
            <div 
                v-for="feature in features"
                :key="feature.title"
                class="onboarding-feature-card"
            >
                <v-icon size="20" class="onboarding-feature-card__icon">{{ feature.icon }}</v-icon>
                <span class="onboarding-feature-card__eyebrow">{{  feature.eyebrow }}</span>
                <h3 class="onboarding-feature-card__title">{{  feature.title }}</h3>
                <p class="onboarding-feature-card__desc">{{  feature.description }}</p>
            </div>
        </div>

        <BaseButton 
            variant="primary" 
            class="onboarding-step_cta" 
            append-icon="mdi-arrow-right"
            @click="$emit('continue')"
        >
            Get Started
        </BaseButton>

        <button type="button" class="onboarding-welcome__skip" @click="$emit('skip')">
            Skip setup and explore
        </button>

        <p class="onboarding-welcome__footer">
            <v-icon size="14">mdi-shield-check-outline</v-icon>
            Official tabletop rulebooks parsed &bull; Boardwise Engine v2.4
        </p>
    </section>
</template>

<script setup>
import BoarleyBubble from './BoarleyBubble.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import OnboardingProgress from './OnboardingProgress.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

defineProps({
    username: { 
        type: String, 
        required: true
    }
})

defineEmits(['continue', 'skip'])

const features = [
    {
        icon: 'mdi-clipboard-list-outline',
        eyebrow: 'Zero Friction',
        title: 'Step-by-Step Setup Wizard',
        description: 'Never get lost in a 40-page rulebook again. Follow curated, interactive board layouts.'
    },
    {
        icon: 'mdi-book-search-outline',
        eyebrow: 'Grounded AI',
        title: 'RAG-Powered Rules Solver',
        description: 'Instant, cited answers grounded in official rulebooks and designer errata in real time.'
    },
    {
        icon: 'mdi-bookshelf',
        eyebrow: 'Curated Play',
        title: 'Personalized Game Library',
        description: 'Tailored game recommendations and shelf management designed specically for your play group.'
    }
]
</script>
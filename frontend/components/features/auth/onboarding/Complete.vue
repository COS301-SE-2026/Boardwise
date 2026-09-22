<template>
    <div class="onboarding-step_final">

        <div class="onboarding-final__header">
            <div class="onboarding-final__eyebrow">
                <v-icon size="14">mdi-check-circle</v-icon>
                ONBOARDING COMPLETE
            </div>
        </div>

        <OnboardingProgress :current="4" :total="4" />

        <section class="onboarding-final__hero">
            <div class="onboarding-final__hero-content">

                <span class="onboarding-final__badge">
                    <v-icon size="12">mdi-auto-fix</v-icon>
                    Boardwise Ready
                </span>

                <h1 class="onboarding-final__title">
                    You're All Set!
                    <span>Let's Play!</span>
                </h1>

                <p class="onboarding-final__description">
                    Your personal Boardwise profile is ready. Your selected games, rulebooks and Setup Wizards are now waiting for you.
                </p>
            </div>

            <div class="onboarding-final__avatar">
                <BaseAvatar 
                    src="/images/Boarley.svg"
                    alt="Boarley"
                    size="xl"
                />

                <span class="onboarding-final__avatar-badge">
                    <v-icon size="12">mdi-check</v-icon>
                </span>
            </div>
        </section>

        <section class="onboarding-final__recommendation">
            <div class="onboarding-final__recommendation-copy">

                <span class="onboarding-final__recommendation-eyebrow">
                    FAST-TRACK RECOMMENDATION
                </span>

                <h2>Ready to unbox tonight?</h2>
                <p>Jump straight into an interactive Setup Wizard. Boarley will guide you through placement,
                    automatically balance resources and cross-check official rulebook instructions in real time.
                </p>
            </div>

            <div class="onboarding-final__actions">
                <BaseButton 
                    variant="primary"
                    size="lg"
                    class="onboarding-final__primary-button"
                    @click="$emit('finished')"
                >
                    Launch Setup Wizard
                    <v-icon size="16" end>
                        mdi-arrow-right
                    </v-icon>
                </BaseButton>

                <button
                    type="button"
                    class="onboarding-final__secondary-button"
                    @click="$emit('finished')"
                >
                    <v-icon size="14">
                        mdi-book-open-outline
                    </v-icon>

                    Go to Rulebook Library
                </button>
            </div>
        </section>

        <section class="onboarding-final__collection">
            <div class="onboarding-final__collection-header">

                <div>
                    <span class="onboarding-final__collection-eyebrow">
                        YOUR COLLECTION
                    </span>

                    <h2>
                        Your Curated Collection
                    </h2>
                </div>

                <p>
                    Rule models are pre-embedded for instant semantic search, component count checks, 
                    and step-by-step setup guides.
                </p>
            </div>

            <div v-if="games.length > 0" class="onboarding-final__game-list">
                <div 
                    v-for="game in games"
                    :key="game.id"
                    class="onboarding-final__game-card"
                >
                    <div class="onboarding-final__game-image">
                        <BaseImage
                            :src="game.imageUrl"
                            :alt="game.title"
                            git="cover"
                        />
                    </div>

                    <div class="onboarding-final__game-info">
                        <span class="onboarding-final__verified">
                            <v-icon size="10">mdi-check-circle</v-icon>
                            Official Rulebook
                        </span>

                        <h3>{{ game.title }}</h3>

                        <p v-if="game.description">{{ game.description }}</p>
                    </div>
                </div>
            </div>

            <div 
                v-else 
                class="onboarding-final__empty"
            >
                <v-icon size="22">mdi-book-open-outline</v-icon>
                <p>No games were added yet.</p>
            </div>
        </section>

        <div class="onboarding-final__finish">
            <BaseButton 
                variant="primary"
                size="lg"
                class="onboarding-final__finish-button"
                @click="$emit('finished')"
            > 
                Enter Boardwise
                <v-icon size="16" end>mdi-arrow-right</v-icon>
            </BaseButton>
        </div>
    </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import OnboardingProgress from './OnboardingProgress.vue'

defineProps({
    games: {
        type:Array, 
        default: () => []
    }
})

defineEmits(['finished'])
</script>
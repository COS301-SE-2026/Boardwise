<template>
    <section
        class="help-ai-section"
        aria-labelledby="help-ai-heading"
    >
        <v-container>
            <BaseCard class="help-ai-card pa-6">
                <div class="help-ai-card__layout">
                    <div class="help-ai-card__mascot">
                        <BaseAvatar
                            src="/images/Boarley_cute.svg"
                            name="Boarley"
                            size="xl"
                        />
                    </div>

                    <div class="help-ai-card__content">
                        <div class="d-flex align-center flex-wrap ga-2 mb-2">
                            <h2
                                id="help-ai-heading"
                                class="help-ai-card__title"
                            >
                                Ask Boarley
                            </h2>

                            <BaseButton
                                color="primary"
                                variant="tonal"
                                size="small"
                            >
                                AI powered
                            </BaseButton>
                        </div>

                        <p class="help-ai-card__description">
                            Confused about a game rule? Choose a rulebook and
                            ask Boarley a question. Answers are grounded in the
                            rulebook so you can check where the answer came from.
                        </p>

                        <div class="d-flex flex-wrap ga-3 mt-5">
                            <BaseButton
                                @click="openRulebookHelp()"
                            >
                                <v-icon
                                    icon="mdi-message-processing-outline"
                                    class="me-2"
                                    aria-hidden="true"
                                />

                                Ask about a game
                            </BaseButton>

                            <BaseButton
                                variant="secondary"
                                @click="router.push('/library')"
                            >
                                Browse rulebooks
                            </BaseButton>
                        </div>

                        <p class="help-ai-card__note">
                            Boarley currently answers questions using available
                            rulebook content.
                        </p>
                    </div>
                </div>
            </BaseCard>
        </v-container>
    </section>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const router = useRouter()

const openRulebookHelp = (question = '') => {
    router.push({
        path: '/rag',
        query: question
            ? {
                ask: question
            }
            : {
                ask: 'true'
            }
    })
}
</script>
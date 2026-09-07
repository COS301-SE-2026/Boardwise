<template>
    <section
        class="landing-rag"
        aria-labelledby="landing-rag-title"
    >
        <v-container>
            <BaseCard class="landing-rag__feature mb-6">
                <div class="landing-rag__grid">

                    <div class="landing-rag__content">

                        <h2
                            id="landing-rag-title"
                            class="landing-rag__title"
                        >
                            Ask Boarley AI.
                            <br><br>
                        </h2>
                        <h3
                        >A rule dispute shouldn't
                            end game night.
                        </h3>
                        
                        <ul class="landing-rag__benefits">
                            <li>
                                <v-icon
                                    icon="mdi-book-open-page-variant"
                                    aria-hidden="true"
                                />

                                <div>
                                    <strong>Rulebook grounded</strong>
                                    <span>Answers use the actual game rules.</span>
                                </div>
                            </li>

                            <li>
                                <v-icon
                                    icon="mdi-source-branch"
                                    aria-hidden="true"
                                />

                                <div>
                                    <strong>Source backed</strong>
                                    <span>See where each answer came from.</span>
                                </div>
                            </li>

                            <li>
                                <v-icon
                                    icon="mdi-lightning-bolt"
                                    aria-hidden="true"
                                />

                                <div>
                                    <strong>Quick answers</strong>
                                    <span>Resolve rule questions without searching pages.</span>
                                </div>
                            </li>
                        </ul>
                    </div>

                    <BaseCard class="landing-rag__tester ">
            <div class="landing-rag__tester-header">
                <div class="landing-rag__tester-identity">
                    <div class="landing-rag__boarley-wrap">
                    <img
                        src="/favicon-boarley.svg"
                        alt=""
                        aria-hidden="true"
                        class="landing-rag__boarley"
                    ></div>

                    <div>
                        <h3>
                            Try Boarley AI
                        </h3>

                        <p class="landing-rag__tester-copy">
                            Pick a rulebook and let Boarley help
                            resolve your next rule dispute.
                        </p>
                    </div>
                </div>
            </div>

            <div class="landing-rag__example-question">
                <p>
                    “Can I move through another player's space?”
                </p>
            </div>

            <p class="landing-rag__hint">
                Answers are grounded in the rulebook
                and include sources you can check.
            </p>

            <BaseButton
                class="landing-rag__try-button"
                @click="router.push('/library')"
            >
                Try Boarley
            </BaseButton>

        </BaseCard>

                </div>
            </BaseCard>
        </v-container>
    </section>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const router = useRouter()

</script>
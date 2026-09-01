<template>
    <section
        class="help-section"
        aria-labelledby="popular-guides-heading"
    >
        <v-container>
            <div class="help-section__header">
                <div>
                    <h2
                        id="popular-guides-heading"
                        class="help-section__title"
                    >
                        Popular features
                    </h2>

                    <p class="help-section__description">
                        Quick answers for common things people do on Boardwise.
                    </p>
                </div>
            </div>

            <div
                v-if="visibleGuides.length"
                class="help-guide-grid"
            >
                <NuxtLink
                    v-for="guide in visibleGuides"
                    :key="guide.id"
                    :to="guide.route"
                    class="help-guide-link"
                >
                    <BaseCard class="help-guide-card pa-5 h-100">
                        <div class="help-guide-card__icon">
                            <v-icon
                                :icon="guide.icon"
                                size="26"
                                aria-hidden="true"
                            />
                        </div>

                        <div class="help-guide-card__content">
                            <div class="d-flex align-center ga-2">
                                <h3 class="help-guide-card__title">
                                    {{ guide.title }}
                                </h3>

                                <v-chip
                                    v-if="guide.ai"
                                    size="x-small"
                                    color="primary"
                                    variant="tonal"
                                >
                                    AI
                                </v-chip>
                            </div>

                            <p class="help-guide-card__description">
                                {{ guide.description }}
                            </p>

                            <span class="help-guide-card__action">
                                View section

                                <v-icon
                                    icon="mdi-arrow-right"
                                    size="18"
                                    aria-hidden="true"
                                />
                            </span>
                        </div>
                    </BaseCard>
                </NuxtLink>
            </div>

            <BaseEmptyState
                v-else
                title="No guides found"
                description="Try another search or select a different help topic."
            />
        </v-container>
    </section>
</template>

<script setup>
import { computed } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { popularGuides } from '~/services/mockData/help'

const props = defineProps({
    search: {
        type: String,
        default: ''
    },

    topic: {
        type: String,
        default: ''
    }
})

const visibleGuides = computed(() => {
    const query = props.search
        .trim()
        .toLowerCase()

    return popularGuides.filter((guide) => {
        const matchesTopic =
            !props.topic ||
            guide.topic === props.topic

        const searchableText = [
            guide.title,
            guide.description,
            guide.topic
        ]
            .join(' ')
            .toLowerCase()

        const matchesSearch =
            !query ||
            searchableText.includes(query)

        return matchesTopic && matchesSearch
    })
})
</script>
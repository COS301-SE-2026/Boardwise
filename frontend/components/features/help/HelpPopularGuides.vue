<template>
  <section
    class="help-section"
    aria-labelledby="popular-guides-heading"
  >
    <v-container>
      <div class="help-section__header mb-5">
        <div>
          <h2
            id="popular-guides-heading"
            class="help-section__title"
          >
            Popular features
          </h2>

          <p class="help-section__description">
            Quick ways to explore Boardwise.
          </p>
        </div>
      </div>

      <v-row v-if="visibleGuides.length">
        <v-col
          v-for="guide in visibleGuides"
          :key="guide.id"
          cols="12"
          sm="6"
          lg="3"
        >
          <NuxtLink
            :to="guide.route"
            :external="guide.route.endsWith('.pdf')"
            :target="guide.route.endsWith('.pdf') ? '_blank' : undefined"
            :rel="guide.route.endsWith('.pdf') ? 'noopener noreferrer' : undefined"
            class="help-guide-link d-block h-100"
          >
            <BaseCard
              class="help-guide-card d-flex align-center ga-4 pa-4 h-100"
            >
              <div class="help-guide-card__icon flex-shrink-0">
                <v-icon
                  :icon="guide.icon"
                  size="24"
                  color="primary"
                  aria-hidden="true"
                />
              </div>

              <div class="help-guide-card__content flex-grow-1">
                <div class="d-flex align-center flex-wrap ga-2">
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

                <p class="help-guide-card__description mb-0 mt-1">
                  {{ guide.description }}
                </p>
              </div>

              <v-icon
                icon="mdi-chevron-right"
                size="22"
                class="help-guide-card__arrow flex-shrink-0"
                aria-hidden="true"
              />
            </BaseCard>
          </NuxtLink>
        </v-col>
      </v-row>

      <BaseEmptyState
        v-else
        title="No guides found"
        message="Try another search or select a different help topic."
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
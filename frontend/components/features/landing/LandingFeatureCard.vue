<template>
    <NuxtLink
        :to="feature.route"
        class="landing-feature-link"
    >
    <div class="landing-feature-card h-100">
  <BaseCard class="pa-6 h-100">
        <div>
        <v-icon
            :icon="feature.icon"
            :size="isHovering ? 74 : 64"
            color="primary"
            aria-hidden="true"
        />
        </div>
        <h3 class="landing-feature-card__title">
            {{ feature.title }}
        </h3>

        <p class="landing-feature-card__description">
            {{ feature.description }}
        </p>

        <ul class="landing-feature-card__highlights">
                <li
                    v-for="highlight in feature.highlights"
                    :key="highlight"
                    class="landing-feature-card__highlight"
                >
                    <v-icon
                        icon="mdi-check-circle"
                        size="18"
                        aria-hidden="true"
                    />

                    <span>
                        {{ highlight }}
                    </span>
                </li>
            </ul>
        
        <div class="landing-feature-card__action">
            
            <span>Explore</span>
            <v-icon 
                icon="mdi-arrow-right"
                size="20"
                aria-hidden="true"
             />
        </div>
    </BaseCard>
    </div>
</NuxtLink>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
    feature: {
        type: Object,
        required: true
    }
})
</script>

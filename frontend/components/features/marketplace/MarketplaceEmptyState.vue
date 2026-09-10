<template>
    <div class="boarley-state">
        <BaseImage
            :src="mascotSrc"
            :alt="copy.title"
            height="160px"
            width="160px"
            fit="contain"
            class="boarley-mascot"
        />

        <h3 class="boarley-state__title">{{  copy.title }}</h3>
        <p class="boarley-state__subtitle">{{  copy.subtitle }}</p>

        <div class="boarley-state__actions">
            <BaseButton
                v-if="showClearFilters"
                variant="secondary"
                @click="$emit('clear-filters')"
            >
                Clear filters
            </BaseButton>

            <BaseButton
                v-if="showCreateListing"
                variant="primary"
                @click="$emit('create-listing')"
            >
                <v-icon star icon="mid-plus" />
                Create the first listing
            </BaseButton>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

import BaseImage from '~/components/ui/BaseImage.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

const props = defineProps({
    tab: { type: String, default: 'Communiy Listings' },
    search: { type: String, default: '' },
    hasActiveFilters: { type: Boolean, default: false },
    mascotSrc: { type: String, default: '/images/BoarleySide.svg' }
})

defineEmits(['clear-filters', 'create-listing'])

const isNarrowed = computed(() => !!props.search || props.hasActiveFilters)

const copy = computed(() => {
    if(props.tab === 'Web') {
        if(isNarrowed.value) {
            return {
                title: props.search
                    ? `No results for "${props.search}"`
                    : 'No results match your filters',
                subtitle: 'Boarley checked every aisle out there and came back empty-handed. Try widening your search.'
            }
        }
        return {
            title: 'Nothing to show yet',
            subititle: "Boarley's still out shopping - check back in a bit, or try a search."
        }
    }

    // Community Listings
    if (isNarrowed.value) {
        return {
        title: props.search
            ? `No listings match "${props.search}"`
            : 'No listings match your filters',
        subtitle: 'Try loosening a filter or two — Boarley promises there are games hiding somewhere.'
        }
    }
    return {
        title: 'The shelf is empty',
        subtitle: "No one has listed a game here yet. Be the first to fill Boarley's shelf!"
    }
})

const showClearFilters = computed(() => isNarrowed.value)
const showCreateListing = computed(() => props.tab === 'Community Listings' && !isNarrowed.value)
</script>

<style scoped>
.boarley-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  padding: 56px 24px;
  width: 100%;
  min-height: 40vh;
}

.boarley-mascot {
  margin-bottom: 4px;
  animation: boarley-tilt 3.5s ease-in-out infinite;
}

.boarley-state__title {
  font-weight: 700;
  color: rgb(var(--v-theme-on-surface));
}

.boarley-state__subtitle {
  color: rgb(var(--v-theme-on-surface));
  opacity: 0.65;
  max-width: 360px;
}

.boarley-state__actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

@keyframes boarley-tilt {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(-4deg); }
}
</style>
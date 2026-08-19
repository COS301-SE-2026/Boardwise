<template>
  <BaseCard>

    <div class="listing-card" @click="openListing">

      <div class="image-container">
        <BaseImage :src="listing.imageUrl" :alt="listing.listingTitle" height="200px" />
        <BaseBadge :variant="listing.listingType === 'rental' ? 'rent' : 'sale'" class="badge">
          {{ listing.listingType === 'rental' ? 'For rent' : 'For sale' }}
        </BaseBadge>
      </div>

      <div class="content">
        <h3>{{ listing.listingTitle }}</h3>
        <p class="price">
          R{{ listing.price }}
        <span v-if="listing.listingType === 'rental' && rentalDates" class="period">
            / {{ rentalDates }}
          </span>
        </p>

        <div class="meta">
            <span class="seller">@{{ listing.username ?? 'unknown' }}</span>
            <span class="location"> {{ listing.location }}</span>
        </div>

      </div>

    </div>

  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
  listing: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const openListing = () => {
  router.push(`/specific-item/${props.listing.id}`)
}

const rentalDates = computed(() => {
  const period = props.listing.rentalPeriod
  if (!Array.isArray(period) || period.length < 2) return null
  return `${period[0]} to ${period[1]}`
})
</script>

<style scoped>
.listing-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
}

.image-container {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.badge {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
}

.price {
  color: var(--color-primary);
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
  margin: 0;
}

.period {
  font-size: var(--fs-small);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.content {
  padding: var(--space-3) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

h3 {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>
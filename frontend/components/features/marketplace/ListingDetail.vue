<template>
  <div class="d-flex flex-wrap ga-10">

    <div class="image-container">
      <img
        :src="listing.imageUrl ?? '/default-listing.png'"
        :alt="listing.gameTitle"
      />
    </div>

    <div class="d-flex flex-column ga-5">

      <div class="d-flex align-center ga-3">
        <h1>{{ listing.gameTitle }}</h1>
        <BaseBadge :variant="listing.listingType === 'rental' ? 'rent' : 'sale'">
          {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
        </BaseBadge>
      </div>

      <p class="price">
        R{{ listing.price }}
        <span v-if="listing.listingType === 'rental'" class="period">
          / {{
            listing.rentalPeriod
              ? `${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate}`
              : 'week'
          }}
        </span>
      </p>

      <div class="d-flex ga-4">
        <span>@{{ listing.username ?? 'unknown' }}</span>
        <span v-if="listing.location">📍 {{ listing.location }}</span>
      </div>

      <div v-if="listing.genres?.length" class="d-flex flex-wrap ga-2">
        <v-chip v-for="genre in listing.genres" :key="genre" size="small">
          {{ genre }}
        </v-chip>
      </div>

      <p class="description">{{ listing.description ?? 'No description provided.' }}</p>

      <v-btn color="primary">Contact Seller</v-btn>

    </div>
  </div>
</template>

<script setup>
import BaseBadge from '~/components/ui/BaseBadge.vue'

defineProps({
  listing: Object
})
</script>

<style scoped>
.image-container {
  background: var(--color-surface-alt);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  height: 400px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

h1 { margin: 0; }

.price {
  color: var(--color-primary);
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  margin: 0;
}

.period {
  font-size: var(--fs-body-lg);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.description {
  color: var(--color-text-muted);
  line-height: var(--lh-relaxed);
  margin: 0;
}
</style>
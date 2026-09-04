<template>
  <div class="d-flex flex-wrap ga-10" data-test="listing-detail">

    <div class="image-container">
      <img
        data-test="listing-image"
        :src="listing.imageUrl ?? '/default-listing.png'"
        :alt="listing.gameTitle"
      />
    </div>

    <div class="d-flex flex-column ga-5">

      <div class="d-flex align-center ga-3">
        <h1 data-test="listing-game-title">{{ listing.gameTitle }}</h1>
        <BaseBadge data-test="listing-badge" :variant="listing.listingType === 'rental' ? 'rent' : 'sale'">
          {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
        </BaseBadge>
      </div>

      <p class="price" data-test="listing-price">
        R{{ listing.price }}
        <span v-if="listing.listingType === 'rental'" class="period" data-test="listing-period">
          / {{
            listing.rentalPeriod
              ? `${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate}`
              : 'week'
          }}
        </span>
      </p>

      <div class="d-flex ga-4">
        <span data-test="listing-username">@{{ listing.username ?? 'unknown' }}</span>
        <span v-if="listing.location" data-test="listing-location"><v-icon>mdi-location</v-icon> {{ listing.location }}</span>
      </div>

      <div v-if="listing.genres?.length" data-test="listing-genre" class="d-flex flex-wrap ga-2">
        <v-chip v-for="genre in listing.genres" :key="genre" size="small">
          {{ genre }}
        </v-chip>
      </div>

      <p class="description" data-test="listing-description">{{ listing.description ?? 'No description provided.' }}</p>

      <BaseButton @click="handleClick" color="primary">Contact Seller</BaseButton>

    </div>
  </div>
</template>

<script setup>
import BaseBadge from '~/components/ui/BaseBadge.vue'
import { useRouter } from 'vue-router'
import BaseButton from '~/components/ui/BaseButton.vue'


const router = useRouter()
const props = defineProps({
  listing: Object
})

const handleClick = () => {
  console.log('Listing.userId: ', props.listing.userId, props.listing)
  router.push({
    path: '/chats',
    query: { newChat: props.listing.userId }
  })
}
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
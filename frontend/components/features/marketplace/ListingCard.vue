<template>
  <BaseCard class="listing-card" @click="openListing">

    <div class="image-container">
      <BaseImage
        :src="listing.imageUrl ?? '/default-listing.png'"
        :alt="listing.gameTitle"
        height="200px"
      />

      <BaseBadge
        class="badge"
        :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
      >
        {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
      </BaseBadge>
    </div>

    <v-card-text class="pa-4 d-flex flex-column ga-2">

      <h2 class="listing-title">
        {{ listing.listingTitle }}
      </h2>

      <h3 class="listing-game">
        {{ listing.gameTitle }}
      </h3>

      <p
        class="price ma-0"
        :style="{ 
          color: listing.listingType === 'rental' 
          ? 'var(--rent)' 
          : 'var(--sale)' 
        }"
      >
        R{{ listing.price }} 
        <br />

        <span v-if="listing.listingType === 'rental'" 
          class="period"
        >
          {{
            listing.rentalPeriod
              ? `${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate}`
              : 'week'
          }}
        </span>
      </p>

      <div class="meta">
        <span>@{{ listing.username ?? 'unknown' }}</span>

        <span v-if="listing.location" class="d-flex align-center ga-1">
          <v-icon size="16"> mdi-map-marker</v-icon> 
          {{ listing.location }}
        </span>
      </div>
    </v-card-text>

  </BaseCard>
</template>

<script setup>
import BaseCard  from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
  listing: { type: Object, required: true }
})

const router = useRouter()

const openListing = () => {
  router.push(`/marketplace/${props.listing.listingId}`)
}
</script>

<style scoped>
.listing-card {
  cursor: pointer;
}

.listing-card:hover {
  transform:  translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.image-container {
  position: relative;
}

.listing-title {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  color: var(--color-primary);
  line-height: 1.2;
  margin: 0;
}

.listing-game {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-semibold);
}

.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}

.period {
  font-size: var(--fs-small);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>
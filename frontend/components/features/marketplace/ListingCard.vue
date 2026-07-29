<template>
  <BaseCard class="listing-card" @click="openListing" style="cursor: pointer">

    <div class="image-container">
      <img
        :src="listing.imageUrl ?? '/default-listing.png'"
        :alt="listing.gameTitle"
      />
      <BaseBadge
        class="badge"
        :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
      >
        {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
      </BaseBadge>
    </div>

    <v-card-text class="d-flex flex-column ga-2 pa-4">
      <h2>{{ listing.listingTitle }}</h2>
      <h3>{{ listing.gameTitle }}</h3>

      <p
        class="price ma-0"
        :style="{ color: listing.listingType === 'rental' ? 'var(--rent)' : 'var(--sale)' }"
      >
        R{{ listing.price }}<br>

        <span v-if="listing.listingType === 'rental'" class="period">
          {{
            listing.rentalPeriod
              ? `${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate}`
              : 'week'
          }}
        </span>
      </p>

      <div class="meta">
        <span>@{{ listing.username ?? 'unknown' }}</span>
        <span v-if="listing.location"><v-icon>
          mdi-map-marker
        </v-icon> {{ listing.location }}</span>
      </div>
    </v-card-text>

  </BaseCard>
</template>

<script setup>
import BaseCard  from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

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
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}
.listing-card:hover {
  transform:  translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

:deep(.badge) {
  color: white;
}
h3 {
  margin: 0;
  font-size: var(--fs-body);
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
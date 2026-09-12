<template>
  <BaseCard data-test="listing-card" clickable @click="openListing">

    <template #media>
      <BaseImage
        data-test="listing-image"
        :src="listing.imageUrl ?? '/default-listing.png'"
        :alt="listing.gameTitle"
        height="200px"
      />

      <BaseBadge
        data-test="listing-badge"
        class="badge--absolute"
        :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
      >
        {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
      </BaseBadge>
    </template>

    <p class="card-title" data-test="listing-title">
      {{ listing.listingTitle }}
    </p>

    <p class="card-subtitle" data-test="listing-game">
      {{ listing.gameTitle }}
    </p>

      <p  
        data-test="listing-price"
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
          class="card-meta"
          data-test="listing-period"
        >
          {{
            listing.rentalPeriod
              ? `${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate}`
              : 'week'
          }}
        </span>
      </p>

      <div class="meta">
        <span class="card-meta" data-test="listing-username">@{{ listing.username ?? 'unknown' }}</span>

        <span v-if="listing.location" data-test="listing-location" class="card-meta d-flex align-center ga-1">
          <v-icon size="16"> mdi-map-marker</v-icon> 
          {{ listing.location }}
        </span>
      </div>

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
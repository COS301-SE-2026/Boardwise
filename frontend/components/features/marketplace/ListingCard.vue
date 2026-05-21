<template>
  <v-card class="listing-card-wrapper" hover @click="openListing">

    <div class="listing-card">

      <div class="image-container">
        <img 
          :src="listing.imageUrl" 
          :alt="listing.gameTitle" />

        <BaseBadge 
          class="badge"
          :variant="listing.listingType === 'rental' ? 'rental' : 'sale'"
        > 
          {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
        </BaseBadge>
      </div>

      <div class="content">

        <h3>{{ listing.gameTitle }}</h3>

        <p class="price">
          R{{ listing.price }}
          <span v-if="listing.type === 'rental'" class="period">
            {{  listing.rentPeriod ?? 'week' }}
          </span>
        </p>

        <div class="meta">
          <span class="seller">@{{ listing.username ?? 'unknown' }}</span>
          <span class="location">📍 Pretoria </span> 
        </div>

      </div>
    </div>
  </v-card>
</template>

<script setup>
import BaseBadge from '~/components/ui/BaseBadge.vue'

const props = defineProps({
  listing: Object
})

const router = useRouter()

const openListing = () => {
  router.push(`/marketplace/${props.listing.listingId}`)
}
</script>

<style scoped>

.listing-card-wrapper {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
}

.listing-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
}

.image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.badge {
  position: absolute;
  top: 10px;
  left: 10px;
}

.content {
  padding: 12px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

h3 {
  margin: 0;
  font-size: 15px;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #888;
}

.price {
color: rgb(var(--v-theme-primary));
  font-weight: 700;
  font-size: 15px;
  margin: 0;
}
</style>
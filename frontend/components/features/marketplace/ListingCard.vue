<template>
  <BaseCard>

    <div class="listing-card" @click="openListing">

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
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
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
.card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
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
  color: #6C3BFF;
  font-weight: 700;
  font-size: 15px;
  margin: 0;
}
</style>
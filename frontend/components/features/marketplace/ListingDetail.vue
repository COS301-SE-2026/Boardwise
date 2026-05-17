<template> 
    <div class="detail">

        <div class="image-container">
            <img 
                :src="/games/catan.jpg" 
                :alt="listing.title" />
        </div>

        <div class="info">
            <div class="title-row">
                <h1>{{ listing.title }}</h1>
                <BaseBadge :variant="listing.type === 'rent' ? 'rent' : 'sale'">
                    {{ listing.type === 'rent' ? 'For Rent' : 'For Sale' }}
                </BaseBadge>
            </div>

            <p class="price">
                R{{ listing.price }}
                <span v-if="listing.type === 'rent'" class="period">/ {{ listing.rentalPeriod ?? 'week' }}</span>
            </p>

            <div class="seller-row">
                <span>@{{ listing.seller ?? 'unknown' }}</span>
                <span>📍 {{ listing.location }}</span>
            </div>

            <p class="description">{{ listing.description ?? 'No description provided.' }}</p>

            <div v-if="listing.negotiable" class="negotiable">
                ✓ Open to negotiation
            </div>

            <BaseButton>Contact Seller</BaseButton>
            </div>

    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

const props = defineProps({
    listing: Object
})
</script>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
}

.image-container {
  background: #f4f4f4;
  border-radius: 12px;
  padding: 20px;
  height: 400px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

h1 { margin: 0; }

.price {
  color: #6C3BFF;
  font-size: 28px;
  font-weight: bold;
  margin: 0;
}

.period {
  font-size: 16px;
  font-weight: 400;
  color: #888;
}

.seller-row {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #888;
}

.description {
  color: #444;
  line-height: 1.6;
  margin: 0;
}

.negotiable {
  font-size: 13px;
  color: #16a34a;
  font-weight: 600;
}
</style>
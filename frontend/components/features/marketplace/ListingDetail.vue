<template> 
  <div class="d-flex flex-wrap ga-10">
        <div class="image-container">
            <img 
                :src="/images/catan.jpg" 
                :alt="listing.title" />
        </div>

        <div class="d-flex flex-column ga-5">
            <div class="d-flex align-center ga-3">
                <h1>{{ listing.title }}</h1>
                <BaseBadge :variant="listing.type === 'rent' ? 'rent' : 'sale'">
                    {{ listing.type === 'rent' ? 'For Rent' : 'For Sale' }}
                </BaseBadge>
            </div>

            <p class="price">
                R{{ listing.price }}
                <span v-if="listing.type === 'rent'" class="period">/ {{ listing.rentalPeriod ?? 'week' }}</span>
            </p>

            <div class="d-flex ga-4">                <span>@{{ listing.seller ?? 'unknown' }}</span>
                <span>📍 {{ listing.location }}</span>
            </div>

            <p class="description">{{ listing.description ?? 'No description provided.' }}</p>

            <div v-if="listing.negotiable" class="negotiable">
                ✓ Open to negotiation
            </div>

            <v-btn color="primary">Contact Seller</v-btn>
            </div>

    </div>
</template>

<script setup>
import BaseBadge from '~/components/ui/BaseBadge.vue'

const props = defineProps({
    listing: Object
})
</script>

<style scoped>

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

h1 { margin: 0; }

.price {
  color: rgb(var(--v-theme-primary));
  font-size: 28px;
  font-weight: bold;
  margin: 0;
}

.period {
  font-size: 16px;
  font-weight: 400;
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
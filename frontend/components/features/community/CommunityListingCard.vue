<template>
  <BaseCard>

    <div class="listing-card" @click="openListing">

      <div class="image-container">
        <img 
          :src="listing.image" 
          :alt="listing.title" />
      </div>

      <div class="content">

        <h3>{{ listing.title }}</h3>

        <div class="meta">
          <p class="price">{{ listing.price }}</p>
          <p class="location">{{ listing.location }}</p>
        </div>

      </div>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  listing: Object
})

const openListing = () => {
  router.push(`/marketplace/${props.id}`)
}
</script>

<style scoped>
.listing-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
}

.image-container {
  height: 220px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f4f4f4;
  border-radius: 12px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

h3 {
  margin: 0;
}

.meta {
  display: flex;
  justify-content: space-between;
}

.price {
  color: #7B2CBF;
  font-weight: bold;
}

.location {
  color: #777;
}
</style>
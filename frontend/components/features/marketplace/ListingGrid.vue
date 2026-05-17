<template> 
    <div class="grid">

        <ListingCard 
            v-for="listing in listings"
            :key="listing.id"
            :listing="listing"
        />
    </div>
</template>

<script setup>
import ListingCard from './ListingCard.vue'
import { listings } from '~/services/mockData/listings'
</script>

<style scoped>
.grid {
    display: grid;

    grid-template-columns:
        repeat(auto-fit, minmax(240px, 1fr)); /* auto wraps to the device (desktop and mobile) */

    gap: 24px;
    width: 100%;
}
</style>
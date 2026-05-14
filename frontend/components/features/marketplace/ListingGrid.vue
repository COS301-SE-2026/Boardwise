<template> 
    <div class="grid">

        <ListingCard 
            v-for="listing in listings"
            :key="listing.id"

            :id="listing.id"
            :title="listing.title"
            :image="listing.image"
            :price="listing.price"
            :location="listing.location"
        />
    </div>
</template>

<script setup>
import ListingCard from './ListingCard.vue'

const listings = [
    {
        id: 1,
        title: 'Catan',
        image: '/games/catan.jpg',
        price: 'R450',
        location: 'Pretoria'
    },

    {
        id:2,
        title: 'Wingspan',
        image: '/games/wingspan.jpg',
        price: 'R700',
        location: 'Cape Town'
    }
]
</script>

<style scoped>
.grid {
    display: grid;

    grid-template-columns:
        repeat(auto-fit, minmax(240px, 1fr)); /* auto wraps to the device (desktop and mobile) */

    gap: 20px;
}
</style>
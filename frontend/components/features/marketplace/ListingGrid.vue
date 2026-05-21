<template> 
    <div class="d-flex flex-wrap ga-6 w-100">


        <ListingCard 
            v-for="listing in listings"
            :key="listing.listingId"
            :listing="listing"
        />
    </div>
</template>

<script setup>
import ListingCard from './ListingCard.vue'
import { listings } from '~/services/mockData/listings'
</script>

<template> 
    <div class="grid">

        <ListingCard 
            v-for="listing in listings"
            :key="listing.listingId"
            :listing="listing"
        />
    </div>
</template>

<script setup>
import BaseGrid    from '~/components/ui/BaseGrid.vue'
import ListingCard from './ListingCard.vue'
defineProps({
    listings: {
        type:Array,
        default: () => []
    }
})
</script>

<style scoped>
.grid {
    display: flex;

    grid-template-columns:
        repeat(auto-fit, minmax(240px, 1fr)); /* auto wraps to the device (desktop and mobile) */

defineProps({
  listings: { type: Array, default: () => [] }
})
</script>
<template>
    <div class="d-flex flex-column ga-5">

        <SectionTitle
            title="Marketplace"
            subtitle="Buy, Rent and List board games with the community"
        />

        <div class="d-flex align-center ga-3 flex-wrap">
            <div class="search">
               <BaseSearch class="flex-grow-1" placeholder="Search for games..." />
            </div>
            <v-btn variant="outlined" color="primary">Filters</v-btn>
            <v-btn color="primary" @click="$emit('create-listing')">+ Create Listing</v-btn>
        </div>

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'

defineEmits(['create-listing'])
</script>
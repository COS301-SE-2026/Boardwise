<template>
  <div class="d-flex flex-column ga-5">

    <SectionTitle
      title="Marketplace"
      subtitle="Buy, Rent and List board games with the community"
    />

    <MarketplaceSearch
     @search="$emit('search', $event)" 
     @create-listing="$emit('create-listing')" 
    />
  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import MarketplaceSearch from './MarketplaceSearch.vue';

defineEmits(['search', 'create-listing'])
</script>
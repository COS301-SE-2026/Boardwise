<template>
  <div class="d-flex flex-column ga-5" data-test="marketplace-header">

    <SectionTitle
      data-test="marketplace-title"
      title="Marketplace"
      subtitle="Buy, Rent and List board games with the community"
    />

    <MarketplaceSearch
      data-test="marketplace-search"
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
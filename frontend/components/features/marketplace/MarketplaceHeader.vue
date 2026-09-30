<template>
  <PageHeader
    data-test="marketplace-header"
    title="Marketplace"
    subtitle="Buy, Rent and Sell board games and more with the community"
    action-label="Create Listing"
    action-icon="mdi-plus"
    @action="$emit('create-listing')"
  >
    <template #search>
      <MarketplaceSearch 
        data-test="marketplace-search"
        @search="$emit('search', $event)" 
        @create-listing="$emit('create-listing')" 
      />
    </template>
  </PageHeader>
</template>

<script setup>
import PageHeader from '~/components/layout/PageHeader.vue'
import MarketplaceSearch from './MarketplaceSearch.vue'

defineEmits(['search', 'create-listing'])
</script>
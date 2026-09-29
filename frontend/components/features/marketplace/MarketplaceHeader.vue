<template>
  <PageHeader
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
import { useDisplay } from 'vuetify'
const { mobile } = useDisplay()

import PageHeader from '~/components/layout/PageHeader.vue'
import MarketplaceSearch from './MarketplaceSearch.vue'

defineEmits(['search', 'create-listing'])
</script>

<style scoped>
.marketplace-header__top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-4);
}
</style>
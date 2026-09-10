<template>
  <div class="d-flex flex-column ga-5" data-test="marketplace-header">
    <div class="marketplace-header__top">
      <SectionTitle
        data-test="marketplace-title"
        title="Marketplace"
        subtitle="Buy, Rent and List board games with the community"
      />

      <BaseButton 
        data-test="create-listing-button" 
        variant="primary" 
        prepend-icon="mdi-plus" 
        @click="$emit('create-listing')"
      > 
          Create Listing
      </BaseButton>
    </div>

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
import BaseButton from '~/components/ui/BaseButton.vue';

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
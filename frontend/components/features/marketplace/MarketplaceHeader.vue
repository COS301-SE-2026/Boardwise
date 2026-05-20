<template>
    <div class="header">

        <SectionTitle
            title="Marketplace"
            subtitle="Buy, Rent and List board games"
        />

        <div class="search-row">
            <div class="search">
                <BaseSearch placeholder="Search for games..." />
            </div>
            <BaseButton variant="secondary"> Filters</BaseButton>
            <BaseButton @click="$emit('create-listing')">+ Create Listing</BaseButton>
        </div>

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineEmits(['create-listing'])
</script>

<style scoped>
.header {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search { flex: 1; }

@media (max-width: 700px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
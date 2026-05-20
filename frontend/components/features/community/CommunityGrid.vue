<template> 
    <div class="community-grid">
    <BaseEmptyState
      v-if="communities.length === 0"
      title="No communities yet"
    />
    <CommunityCard
      v-for="item in communities"
      :key="item.id"
      :community="item"
      @join="$emit('join-request', item.id)"
    />
  </div>
</template>

<script setup>
import CommunityCard from './CommunityCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
    communities: {
        type: Array,
        default: () => [],
    }
})
</script>

<style scoped>
.community-grid {
    display: grid;

    grid-template-columns:
        repeat(auto-fit, minmax(280px, 1fr)); 

    gap: 20px;
}
</style>
<template> 
  <BaseGrid
    cols="280px"
    gap="20px"
  >
    <BaseEmptyState
      v-if="communities.length === 0"
      title="No communities yet"
      class="justify-center"
    />
    <CommunityCard
      v-for="item in communities"
      :key="item.id"
      :community="item"
      @join="$emit('join-request', item.id)"
    />
  </BaseGrid>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue';
import CommunityCard from './CommunityCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
    communities: {
        type: Array,
        default: () => [],
    }
})
</script>
<!-- 
<style scoped>
.community-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-5);
  align-items: start;
}

@media (max-width: 900px) {
  .community-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 600px) {
  .community-grid {
    grid-template-columns: 1fr;
  }
}
</style> -->
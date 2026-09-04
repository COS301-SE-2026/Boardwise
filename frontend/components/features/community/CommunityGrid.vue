<template> 
    <BaseEmptyState
      v-if="communities.length === 0"
      title="No communities yet"
      class="justify-center"
    />

  <BaseGrid
    v-else
    cols="280px"
    gap="20px"
  >
    <CommunityCard
      v-for="community in communities"
      :key="community.id"
      :community="community"
      @join="$emit(community.id)"
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

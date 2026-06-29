<template>
  <BaseGrid cols="280px" gap="24px">
    <BaseEmptyState
      v-if="communities.length === 0"
      title="No communities found"
      message="Try adjusting your search or create a new community"
      class="empty-state"
    />
    
    <CommunityCard
      v-for="community in communities"
      :key="community.id"
      :community="community"
      @click="handleView(community.id)"
    />
  </BaseGrid>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import CommunityCard from './CommunityCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
  communities: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['join-request', 'view-community'])

const handleView = (communityId) => {
  emit('view-community', communityId)
}
</script>

<style scoped>
.empty-state {
  grid-column: 1 / -1;
  padding: var(--space-10) 0;
}
</style>
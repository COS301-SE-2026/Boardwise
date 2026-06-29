<template>
  <div class="member-list">
    <div class="member-header">
      <h2>
        <v-icon left>mdi-account-group</v-icon>
        Members
      </h2>
      <BaseButton @click="showInviteModal = true">
        <v-icon left>mdi-account-plus</v-icon>
        Invite Member
      </BaseButton>
    </div>

    <div class="member-search">
      <BaseSearch v-model="searchQuery" placeholder="Search members..." />
      <span class="member-count">{{ filteredMembers.length }} members</span>
    </div>

    <BaseGrid cols="220px" gap="16px">
      <MemberCard 
        v-for="member in filteredMembers" 
        :key="member.id" 
        :member="member" 
      />
    </BaseGrid>

    <!-- Invite Modal -->
    <InviteMemberModal
      v-model="showInviteModal"
      @confirm="handleInvite"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import MemberCard from './MemberCard.vue'
import InviteMemberModal from './InviteMemberModal.vue'

const props = defineProps({
  community: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['invite'])

const searchQuery = ref('')
const showInviteModal = ref(false)

const filteredMembers = computed(() => {
  if (!props.community.members_list) return []
  if (!searchQuery.value) return props.community.members_list
  
  const query = searchQuery.value.toLowerCase()
  return props.community.members_list.filter(member =>
    member.name.toLowerCase().includes(query)
  )
})

const handleInvite = (invite) => {
  emit('invite', invite)
}
</script>

<style scoped>
.member-list {
  padding: var(--space-2) 0;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.member-header h2 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--fs-h3);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  margin: 0;
}

.member-search {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.member-search .base-search {
  flex: 1;
}

.member-count {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .member-header {
    flex-direction: column;
    gap: var(--space-3);
    align-items: stretch;
  }

  .member-search {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
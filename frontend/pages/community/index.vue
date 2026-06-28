<template>
     <PageContainer>
    <div class="community-page">
      <div class="community-header">
        <SectionTitle title="Communities" subtitle="Join a community to connect with others"/>
        
        <div class="community-actions">
          <BaseSearch v-model="searchQuery" placeholder="Find a community..." />
          <BaseTag :tabs="['All', 'My groups', 'Trending']" v-model="activeTab" />
        </div>
      </div>

      <CommunityGrid 
        :communities="filteredCommunities"
        @join-request="handleJoin"
        @view-community="handleViewCommunity"
      />

      <v-btn
        color="primary"
        size="large"
        rounded="pill"
        class="create-btn"
        @click="showCreateModal = true"
      >
        <v-icon left>mdi-plus</v-icon>
        Create Community
      </v-btn>

      <CommunityCreateForm
        v-model="showCreateModal"
        @confirm="handleCreate"
      />
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseTag from '~/components/ui/BaseTag.vue'
import CommunityGrid from '~/components/features/community/explore/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/models/CommunityCreateForm.vue'
import { communities as mockCommunities } from '~/mockdata/communities.js'


const router = useRouter()
const searchQuery = ref('')
const activeTab = ref('All')
const showCreateModal = ref(false)

onMounted(() => {
  console.log(' Communities loaded:', mockCommunities)
  console.log(' Total communities:', mockCommunities.length)
  console.log(' First community:', mockCommunities[0])
})
const communities = ref(mockCommunities)

const filteredCommunities = computed(() => {
  let result = communities.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(community =>
      community.name.toLowerCase().includes(query) ||
      community.description.toLowerCase().includes(query)
    )
  }
  
  if (activeTab.value === 'My groups') {
      result = result.filter(c => 
      c.members_list?.some(m => m.name === 'You' || m.role === 'Member')
    )
  } else if (activeTab.value === 'Trending') {
    result = [...result].sort((a, b) => b.members - a.members)
  }
  
  return result
})

const handleJoin = (communityId) => {
  const community = communities.value.find(c => c.id === communityId)
  if (community) {
    const isMember = community.members_list?.some(m => m.name === 'You')
    
    if (isMember) {
      community.members -= 1
      community.members_list = community.members_list.filter(m => m.name !== 'You')
    } else {
      community.members += 1
      if (!community.members_list) community.members_list = []
      community.members_list.push({
        id: Date.now(),
        name: 'You',
        role: 'Member',
        avatar: '/images/avatar.jpg'
      })
    }
  }
}

const handleViewCommunity = (communityId) => {
  router.push(`/community/${communityId}`)
}

const handleCreate = (newCommunity) => {
  communities.value.unshift({
    ...newCommunity,
    id: communities.value.length + 1,
    members: 1,
    members_list: [
      { id: 1, name: 'You', role: 'Admin', avatar: '/images/avatar.jpg' }
    ]
  })
  showCreateModal.value = false
}
</script>

<style scoped>.community-page {
  padding-bottom: 80px;
}

.community-header {
  margin-bottom: var(--space-6);
}

.community-actions {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  margin-top: var(--space-4);
  flex-wrap: wrap;
}

.community-actions .base-search {
  flex: 1;
  min-width: 200px;
}

.create-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 100;
  box-shadow: var(--shadow-lg);
}

@media (max-width: 768px) {
  .community-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .create-btn {
    bottom: 80px;
    right: 16px;
  }
}
</style>

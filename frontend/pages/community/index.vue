<template>
  <PageContainer>
    <Navbar />

    <div class="community-page">
      <SectionTitle title="Communities" subtitle="Join a community to connect with others" />

      <div class="community-toolbar">
        <BaseSearch v-model="searchQuery" placeholder="Find a community..." class="community-search" />
        <BaseButton variant="primary" @click="showCreateModal = true" class="create-btn">
          <v-icon left size="18">mdi-plus</v-icon>
          Create Community
        </BaseButton>
      </div>

      <div class="community-layout">
        <aside class="community-sidebar">
          <CommunityFilter 
            :active-tab="activeTab"
            @filter-change="handleFilterChange"
          />
        </aside>

        <main class="community-main">
          <div class="filter-tabs">
            <BaseTag 
              :tabs="['All', 'My groups', 'Trending']" 
              v-model="activeTab" 
            />
            <span class="community-count">{{ filteredCommunities.length }} communities</span>
          </div>

          <CommunityGrid 
            :communities="filteredCommunities"
            @join-request="handleJoin"
            @view-community="handleViewCommunity"
          />
        </main>
      </div>

      <CommunityCreateForm
        v-model="showCreateModal"
        @confirm="handleCreate"
      />
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseTag from '~/components/ui/BaseTag.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import CommunityGrid from '~/components/features/community/explore/CommunityGrid.vue'
import CommunityFilter from '~/components/features/community/feed/CommunityFilter.vue'
import CommunityCreateForm from '~/components/features/community/models/CommunityCreateForm.vue'
import { communities as mockCommunities } from '~/services/mockData/communities.js'

const router = useRouter()
const searchQuery = ref('')
const activeTab = ref('All')
const showCreateModal = ref(false)

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

<style scoped>
.community-page {
  padding-bottom: var(--space-8);
}

.community-toolbar {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  margin: var(--space-4) 0 var(--space-6);
}

.community-search {
  flex: 1;
  max-width: 400px;
}

.create-btn {
  font-family: var(--font-button);
  font-weight: var(--fw-bold);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-5);
}

.community-layout {
  display: flex;
  gap: var(--space-6);
  align-items: flex-start;
}

.community-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.community-main {
  flex: 1;
  min-width: 0;
}

.filter-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.community-count {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .community-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .community-search {
    max-width: none;
  }

  .community-layout {
    flex-direction: column;
  }

  .community-sidebar {
    width: 100%;
  }

  .filter-tabs {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-2);
  }
}
</style>

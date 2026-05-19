<template>
  <PageContainer>
 
    <Navbar />
 
    <ExploreHeader />
 
    <ExploreSearch v-model="searchQuery" />
 
    <ExploreTabs @change="activeTab = $event" />
    <div class="community-layout">
      <CommunityFilter @filter="handleFilter" />
      <CommunityGrid :communities="filteredCommunities" />
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { communities } from '~/services/mockData/communities'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
 
import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'
import ExploreTabs from '~/components/features/community/ExploreTabs.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'

const searchQuery = ref('')
const activeTab = ref('All')
const selectedTypes = ref([])
const selectedCategories = ref([])

const handleFilter = ({ types, categories }) => {
  selectedTypes.value = types
  selectedCategories.value = categories
}
const filteredCommunities = computed(() =>
  communities.filter(c => {
    const matchesSearch = c.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesTab = activeTab.value === 'All' || c.type === activeTab.value
    const matchesType = selectedTypes.value.length === 0 || selectedTypes.value.includes(c.type)
    const matchesCategory = selectedCategories.value.length === 0 || selectedCategories.value.includes(c.category)
    return matchesSearch && matchesTab && matchesType && matchesCategory
  })

)

</script>

<style scoped>
.community-layout {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  align-items: flex-start;
}

@media (max-width: 900px) {
  .community-layout {
    flex-direction: column;
  }
}
</style>
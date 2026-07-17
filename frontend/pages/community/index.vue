<template>
  <PageContainer>
    <Navbar />
 
    <ExploreHeader />
 
    <ExploreSearch 
      v-model="searchQuery"
      @create-community="showCreateCommunity = true"
    />
 
    <CommunityGrid 
        class="mt-6"
        :communities="filteredCommunities"
      />

      <CommunityFilter 
        class="mt-6"
        @filter="handleFilter" 
      />

    <CommunityCreateForm 
      v-model="showCreateCommunity"
    />

  </PageContainer>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted } from 'vue'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'

import { useCommunity } from '~/composables/useCommunity'

const { communities, getAllCommunities, } = useCommunity()

const searchQuery = ref('')
const showCreateCommunity = ref(false)

const selectedTypes = ref<string[]>([])
const selectedCategories = ref<string[]>([])

onMounted(() => {
  getAllCommunities()
})

const handleFilter = ({
  types,
  categories
}: {
  types: string[]
  categories: string[]
}) => {
     selectedTypes.value = types
     selectedCategories.value = categories
}

const filteredCommunities = computed(() => {
  return communities.value.filter(community => {
    const matchesSearch =
      community.name
        .toLowerCase()
        .includes(searchQuery.value.toLowerCase())

    const matchesVisibility =
      selectedTypes.value.length === 0 ||
      selectedTypes.value.includes(community.category)

    const matchesCategory =
      selectedCategories.value.length === 0 ||
      selectedCategories.value.includes(community.category)

    return matchesSearch  && matchesVisibility && matchesCategory
  })
})

// // 3. Filter the reactive array synchronously
// const filteredCommunities = computed(() => {
//  // 3.1. Fail-safe: If data hasn't loaded, return an empty array
  // if (!communities.value) return []

  // return communities.value.filter(c => {
//  // 3.2. Safe Name Search (Ensures c.name exists before running toLowerCase)
    // const matchesSearch = c.name 
        // ? c.name.toLowerCase().includes(searchQuery.value.toLowerCase()) 
        // : false
    
    // // 3.3. Tab Filter (Checks against c.type, defaults to true if Tab is "All")
    // const matchesTab = activeTab.value === 'All' || c.type === activeTab.value
    
//     // 3.4. Array Filters (Passes automatically if no checkboxes are selected)
//     const matchesType = selectedTypes.value.length === 0 || selectedTypes.value.includes(c.type)
//     const matchesCategory = selectedCategories.value.length === 0 || selectedCategories.value.includes(c.category)
    
//     return matchesSearch && matchesTab && matchesType && matchesCategory
//   })
// })

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

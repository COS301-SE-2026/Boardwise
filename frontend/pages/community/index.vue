<template>
  <PageContainer>
 
    <Navbar />
 
    <ExploreHeader />
 
    <ExploreSearch 
      v-model="searchQuery"
      @create-community="showCreateCommunity = true"
    />
 
    <div class="community-layout">
      <CommunityFilter @filter="handleFilter" />
      <CommunityGrid :communities="filteredCommunities" />
    </div>

    <CommunityCreateForm 
      v-model="showCreateCommunity"
    />

  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { communities } from '~/services/mockData/communities'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'
// 1. Destructure the composable
// const { communities, getAllCommunities, loading, error } = useCommunity()

const searchQuery = ref('')
const activeTab = ref('All')
const selectedTypes = ref([])
const selectedCategories = ref([])
const showCreateCommunity = ref(false)
const router = useRouter()

onMounted(() => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
  }
})
// 2. Trigger the fetch when the page loads
// onMounted(() => {
//   getAllCommunities()
// })

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

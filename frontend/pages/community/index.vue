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
      @confirm="handleCreate"
    />

  </PageContainer>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'
import type { GroupInfo } from '~/services/communityService'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'


const { getAllCommunities, searchForCommunity } = useCommunity()
const { show } = useSnackBar()

const searchQuery = ref('')
const showCreateCommunity = ref(false)
const communities = ref<Array<GroupInfo>>([])

const selectedTypes = ref<string[]>([])
const selectedCategories = ref<string[]>([])

onMounted(async () => {
  communities.value = await getAllCommunities()
})

const delaySearch = useDebounceFn( async (query) => {
  const res = await searchForCommunity(query)
  communities.value = Array.isArray(res) ? res : []
}, 400)

watch(searchQuery, (query) => {
  delaySearch(query) 
})

const handleCreate = (newCommunity: GroupInfo) => {
  communities.value.push(newCommunity)
  show("Community successfully created")
}

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
    const matchesVisibility =
      selectedTypes.value.length === 0 ||
      selectedTypes.value.includes(community.visibility.toLowerCase())

    const matchesCategory =
      selectedCategories.value.length === 0 ||
      selectedCategories.value.includes(community.category.toLowerCase())

    return matchesVisibility && matchesCategory
  })
})

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

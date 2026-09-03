<template>
  <PageContainer>
    <Navbar />
 
    <ExploreHeader />
 
    <ExploreSearch 
      v-model="searchQuery"
      @create-community="showCreateCommunity = true"
    />

    <!-- Mobile filter trigger -->
<div class="d-flex d-md-none mt-6 mb-4">
  <v-chip
    color="secondary"
    prepend-icon="mdi-filter-variant"
    size="large"
    :aria-expanded="showFilters"
    aria-controls="community-mobile-filters"
    @click="showFilters = true"
  >
    Filters
  </v-chip>

  <v-navigation-drawer
    v-model="showFilters"
    temporary
    location="left"
    width="300"
  >
    <div
      id="community-mobile-filters"
      class="pa-4"
    >
      <CommunityFilter
        @filter="handleFilter"
      />
    </div>
  </v-navigation-drawer>
</div>

<!-- Shared catalogue/results -->
<div class="community-results-layout">

  <!-- Desktop filters -->
  <aside
    class="community-results-layout__filters d-none d-md-block"
    aria-label="Community filters"
  >
    <CommunityFilter
      @filter="handleFilter"
    />
  </aside>

  <!-- One results area for desktop + mobile -->
  <div class="community-results-layout__content">

    <div
      v-if="loading"
      class="community-results-loading"
      role="status"
      aria-label="Loading communities"
    >
      <v-progress-circular
        indeterminate
        color="primary"
        size="48"
      />
    </div>

    <CommunityGrid
      v-else
      :communities="filteredCommunities"
    />

  </div>
</div>

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


const { getAllCommunities, searchForCommunity, loading } = useCommunity()
const { show } = useSnackBar()

const searchQuery = ref('')
const showCreateCommunity = ref(false)
const communities = ref<Array<GroupInfo>>([])

const selectedTypes = ref<string[]>([])
const selectedCategories = ref<string[]>([])

onMounted(async () => {
  communities.value = await getAllCommunities()
})
const showFilters = ref(false)
const delaySearch = useDebounceFn( async (query) => {
  const res = await searchForCommunity(query)
  communities.value = Array.isArray(res) ? res : []
}, 400)

watch(searchQuery, (query) => {
  delaySearch(query) 
})

const handleCreate = (newCommunity: GroupInfo) => {
  communities.value.push(newCommunity)
  show("Your community is ready. Welcome to the table!")
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

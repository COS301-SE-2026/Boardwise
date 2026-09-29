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
    class="community-results-layout__filters d-flex d-md-block"
    aria-label="Community filters"
  >
    <CommunityFilter
      @filter="handleFilter"
    />
  </aside>

  <!-- One results area for desktop + mobile -->
  <div class="community-results-layout__content">

    <output
      v-if="loading"
      class="community-results-loading"
      aria-live="polite"
      aria-label="Loading communities"
    >
      <v-progress-circular
        indeterminate
        color="primary"
        size="48"
      />
    </output>

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

import { ref, computed} from 'vue'

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
import { useDebouncedAutocomplete } from '~/composables/useDebounce'

const { getAllCommunities, searchForCommunity, loading } = useCommunity()
const { show } = useSnackBar()

const showCreateCommunity = ref(false)
const selectedTypes = ref<string[]>([])
const selectedCategories = ref<string[]>([])
const showFilters = ref(false)

const fetchCommunitiesData = async (query: string): Promise<GroupInfo[]> => {
  if (!query || !query.trim()){
    return await getAllCommunities();
  }

  const res = await searchForCommunity(query.trim());
  return Array.isArray(res) ? res : []
}

const {search: searchQuery, options: communities} = useDebouncedAutocomplete(fetchCommunitiesData, {debounceMs: 400, fetchOnMount: true});

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
.community-results-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  margin-top: 24px;
}

.community-results-layout__filters {
  flex: 0 0 260px;
  width: 260px;
}

.community-results-layout__content {
  flex: 1;
  min-width: 0;
}

.community-results-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
}

@media (max-width: 900px) {
  .community-results-layout {
    flex-direction: column;
  }

  .community-results-layout__filters {
    display: none; /* desktop-only aside; mobile uses the drawer */
  }
}
</style>

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

    <BaseLoadingState v-if="loading" />

    <template v-else>
      <CommunityGrid
        :communities="pagedCommunities"
      />

      <template v-if="filteredCommunities.length > 0">
        <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
          <span class="card-meta">Page {{ communitiesPage }} of {{ communitiesTotalPages }}</span>
        </div>

        <BasePagination
          v-if="communitiesTotalPages > 1"
          class="mt-4"
          :model-value="communitiesPage"
          :total-pages="communitiesTotalPages"
          @update:modelValue="goToCommunitiesPage"
        />
      </template>
    </template>
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
import BasePagination from '~/components/ui/BasePagination.vue'

import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'
import type { GroupInfo } from '~/services/communityService'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

const CARD_PAGE_SIZE = 6

const { getAllCommunities, searchForCommunity, loading } = useCommunity()
const { show } = useSnackBar()

const searchQuery = ref('')
const showCreateCommunity = ref(false)
const communities = ref<Array<GroupInfo>>([])

const selectedTypes = ref<string[]>([])
const selectedCategories = ref<string[]>([])

onMounted(async () => {
  communities.value = await getAllCommunities()
  console.log(communities.value)
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

// =================== Pagination ============================
const communitiesPage = ref(1)

const communitiesTotalPages = computed(() => 
  Math.max(1, Math.ceil(filteredCommunities.value.length / CARD_PAGE_SIZE))
)

const pageCommunities = computed(() => {
  const start = (communitiesPage.value - 1) * CARD_PAGE_SIZE
  return filteredCommunities.value.slice(start, start + CARD_PAGE_SIZE)
})

const goToCommunitiesPage = (page: number) => {
  communitiesPage.value = page
}

watch(filteredCommunities, () => {
  if (communitiesPage.value > communitiesTotalPages.value) {
    communitiesPage.value = 1
  }
})

const pagedCommunities = computed(() => {
  const start = (communitiesPage.value - 1) * CARD_PAGE_SIZE
  return filteredCommunities.value.slice(start, start + CARD_PAGE_SIZE)
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

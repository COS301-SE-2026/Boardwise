<template>
  <PageContainer>
    <Navbar />
 
    <ExploreHeader 
      v-model:search-query="searchQuery"
      @create-community="showCreateCommunity = true"  
    />

    <SocialTabs data-test="social-tabs" v-model="activeTab" />

    <template v-if="activeTab === 'Communities'">
      <!-- Mobile filter trigger -->
      <MobileFilterDrawer id="community-mobile-filters">
        <CommunityFilter @filter="handleFilter" />
      </MobileFilterDrawer>

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
    </template>

    <template v-else-if="activeTab === 'Friends'">
      <MobileFilterDrawer id="friend-mobile-filters">
        <FriendsFilterSidebar @filter="handleFriendFilter" />
      </MobileFilterDrawer>

      <!-- Desktop -->
      <div class="d-flex d-md-flex ga-6 mt-6 align-start">
        <div class="d-none d-md-block">
          <FriendsFilterSidebar 
            data-test="friend-filter"
            @filter="handleFriendFilter"
          />
        </div>

        <div class="flex-1-1">
          <!-- Loading -->
          <div
            v-if="isLoading"
            class="d-flex justify-center align-center"
            style="min-height: 60vh"
          >
            <BaseLoadingState />
          </div>

          <!-- Empty -->
          <BaseEmptyState
            v-else-if="filteredPeople.length === 0"
            title="No people found"
            description="Try changing your search or friend filters."
          />

          <!-- People grid -->
          <template v-else>
            <PeopleGrid 
              data-test="people-grid"
              :people="pagedPeople"
              variant="discover"
              @add-friend="handleAddFriend"
              @message="handleMessage"
              @unfriend="handleUnfriend"
            />

            <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
              <span class="card-meta">
                Showing {{ friendRangeStart }}-{{ friendRangeEnd }}
                of {{ filteredPeople.length }} tabletop players
              </span>
            </div>

            <BasePagination 
              v-if="friendTotalPages > 1"
              class="mt-4"
              :model-value="friendsPage"
              :total-pages="friendTotalPages"
              @update:model-value="goToFriendsPage"
            />
          </template>
        </div>
      </div>
    </template>
    
    

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
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import MobileFilterDrawer from '~/components/ui/MobileFilterDrawer.vue'

import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'
import SocialTabs from '~/components/features/social/SocialTabs.vue'
import type { GroupInfo } from '~/services/communityService'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'

const CARD_PAGE_SIZE = 6

const { getAllCommunities, searchForCommunity, loading } = useCommunity()
const { show } = useSnackBar()

const activeTab = ref('Friends')
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

// ======================= Friends ============================== 
import FriendsFilterSidebar from '~/components/features/people/FriendsFilterSidebar.vue'
import PeopleGrid from '~/components/features/people/PeopleGrid.vue'

import { useFriends } from '~/composables/useFriends'
import { useRouter } from 'vue-router'

const router = useRouter()

const {
    userFriendList,
    isLoading,
    sendFriendRequest,
    unfriendUser
} = useFriends()

const showFriendFilters = ref(false)
const selectedFriendStatuses = ref<string[]>([])
const friendsPage = ref(1)

const FRIENDS_PAGE_SIZE = 9

const mockPeople = ref([
  {
    id: 'mock-1',
    username: 'meeplemaster',
    fullname: 'Sarah Johnson',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-2',
    username: 'dicequeen',
    fullname: 'Emily Williams',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-3',
    username: 'boardgamer42',
    fullname: 'James Smith',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-4',
    username: 'cardboardking',
    fullname: 'Daniel Brown',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-5',
    username: 'tabletopgirl',
    fullname: 'Jessica Adams',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-6',
    username: 'rollwithit',
    fullname: 'Michael Jones',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-7',
    username: 'meeplewizard',
    fullname: 'Olivia Davis',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-8',
    username: 'diceanddragons',
    fullname: 'Matthew Wilson',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-9',
    username: 'boardqueen',
    fullname: 'Sophie Taylor',
    profilePicture: '/images/avatar.jpg'
  },
  {
    id: 'mock-10',
    username: 'sweeyyy',
    fullname: 'Swelihle Makhankiti',
    profilePicture: '/images/avatar.jpg'
  }
])

// onMounted(async () => {
//   await getOwnFriendsList()
// })

const filteredPeople = computed(() => {
  // let result = userFriendList.value?.friends ?? []
  let result = mockPeople.value
  
  const query = searchQuery.value.trim().toLowerCase()

  if (query) {
    result = result.filter(person =>
        person.username?.toLowerCase().includes(query) ||
        person.fullname?.toLowerCase().includes(query)
    )
  }

  return result
})

const friendTotalPages = computed(() => {
  return Math.max(1,
    Math.ceil(filteredPeople.value.length / FRIENDS_PAGE_SIZE)
  )
})

const pagedPeople = computed(() => {
  const start = (friendsPage.value - 1) * FRIENDS_PAGE_SIZE

  return filteredPeople.value.slice(
    start, 
    start + FRIENDS_PAGE_SIZE
  )
})

const handleMessage = (id: string) => {
    router.push({
        path: '/chats',
        query: {
            newChat: id
        }
    })
}

const handleAddFriend = async (id: string) => {
  await sendFriendRequest(id)
}

const handleUnfriend = async (id: string) => {
  await unfriendUser(id)
  // await getOwnFriendsList()
}

const friendRangeStart = computed(() => {
  if (filteredPeople.value.length === 0) {
    return 0
  }

  return (friendsPage.value - 1) * FRIENDS_PAGE_SIZE + 1
})

const friendRangeEnd = computed(() => {
    return Math.min(
        friendsPage.value * FRIENDS_PAGE_SIZE,
        filteredPeople.value.length
    )
})

const goToFriendsPage = (page: number) => {
    friendsPage.value = page
}

watch(filteredPeople, () => {
    if (friendsPage.value > friendTotalPages.value) {
        friendsPage.value = 1
    }
})

const handleFriendFilter = ({
    statuses
}: {
    statuses: string[] | null
}) => {
    selectedFriendStatuses.value = statuses ?? []
    friendsPage.value = 1
}
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

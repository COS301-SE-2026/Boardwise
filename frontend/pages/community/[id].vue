<template>
  <PageContainer>
    <Navbar />

    <v-container
      class="community-detail-page"
    >
    <div
        class="community-layout"
        :class="{
          'community-layout--community-open':
            mobileCommunityOpen
        }"
      >
        <aside
          class="community-layout__sidebar"
          aria-label="Communities"
        >
          <CommunitySidebar
            :communities="communities"
            :selected-id="route.params.id"
            :loading="communitiesLoading"
            @select="handleCommunitySelect"
          />
        </aside>

        <main class="community-layout__main">
          <BaseButton
            variant="secondary"
            class="community-layout__mobile-back"
            @click="mobileCommunityOpen = false"
          >
            <v-icon
              icon="mdi-arrow-left"
              class="me-2"
              aria-hidden="true"
            />

            Communities
          </BaseButton>

          <output
            v-if="detailsLoading"
            class="community-detail-loading"
            aria-live="polite"
            aria-label="Loading community"
          >
            <v-progress-circular
              indeterminate
              color="primary"
              size="48"
            />
          </output>
      <section
        v-else-if="community"
        class="community-chat-window"
        :aria-label="`${community.name} community chat`"
      >
 
      <CommunityBanner 
        :community="community" 
        @details="showDetails = true"
        @updated="handleUpdate"
      />
 
      <CommunityChats 
        :community="community" 
        :token="token"
        @join="handleJoin"
      />
    </section>

    <BaseEmptyState
            v-else
            title="Community not found"
            description="This community may no longer be available."
          />
        </main>
      </div>

      <CommunityMoreDetails
        v-if="community"
        v-model="showDetails"
        :community="community"
        :loading="detailsLoading"
        @leave="handleLeave"
      />
    </v-container>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityMoreDetails from '~/components/features/community/CommunityMoreDetails.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import CommunitySidebar from '~/components/features/community/CommunitySidebar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'

const route = useRoute()
const router = useRouter()

const {
  getCommunityDetails,
  joinCommunity,
  leaveCommunity,
  error,
  loading: detailsLoading
} = useCommunity()

const {
  getAllCommunities,
  loading: communitiesLoading
} = useCommunity()

const { show } = useSnackBar()

const community = ref(null)
const token = ref('')
const showDetails = ref(false)
const communities = ref([])
const mobileCommunityOpen = ref(false)

onMounted(async () => {
  const rawToken = localStorage.getItem("access_token")
  if (!rawToken) {
    await router.push('/auth/signin')
    return
  }

  token.value = rawToken

  const response = await getAllCommunities()

  community.value = await getCommunityDetails(route.params.id)

  communities.value =
    response?.data?.groups ??
    response?.data ??
    response?.groups ??
    response ??
    []
 
})

const handleCommunitySelect = async (id) => {
  mobileCommunityOpen.value = true

  if (String(id) !== String(route.params.id)) {
    await router.push(`/community/${id}`)
  }
}

const handleJoin = async () => {
  try {
    const response = await joinCommunity(route.params.id)

    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember

    show('Nice move! You joined the community.', 'success')
  } catch (err) {
    console.error('Failed to join community.', err)
    show(error.value, 'error')
  }
}

const handleLeave  = async () => {
  try {
    const response = await leaveCommunity(route.params.id)

    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember

    show('You left the community.', 'success')
    showDetails.value = false
  } catch (err) {
    console.error('Failed to leave the community.', err)
    show(error.value || 'Could not leave the community.', 'error')
  }
}

const handleUpdate = (newData) => {
  if (!newData || !community.value) return

  community.value.name = newData.name
  community.value.description = newData.description
  community.value.visibility = newData.visibility
  community.value.imageUrl = newData.imageUrl

  show('Nice move! Community details updated.', 'success')
}

watch(
  () => route.params.id,
  async (id) => {
    if (!id || !token.value) return

    showDetails.value = false
    community.value = await getCommunityDetails(id)
  },
  { immediate: true }
)
</script>

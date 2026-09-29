<template>
  <PageContainer>
    <Navbar />

    <v-container
      class="community-detail-page"
    >
    <div
        class="community-detail-layout"

      >

        <main class="community-detail-layout__main">
          <div class="community-layout__mobile-back">
          <BaseBackButton to="/social">
            <v-icon
              icon="mdi-arrow-left"
              size="20"
              class="me-2"
              aria-hidden="true"
            />
            Communities
          </BaseBackButton>
        </div>

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
        v-else-if="community && !isRestrictedPrivateCommunity"
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
      v-else-if="isRestrictedPrivateCommunity"
      title="Private community"
      message="You need approval from the owner before you can view this community."
    />

    <BaseEmptyState
            v-else
            title="Community not found"
            description="This community may no longer be available."
          />
        </main>
      </div>

      <CommunityMoreDetails
        v-if="community && !isRestrictedPrivateCommunity"
        v-model="showDetails"
        :community="community"
        :loading="detailsLoading"
        @leave="handleLeave"
      />
    </v-container>

    <output
      v-if="!community && loading"
      class="community-detail-loading"
      aria-live="polite"
      aria-label="Loading community"
    >
      <BaseSpinner size="lg" />
    </output>

    <BaseEmptyState
      v-if="!community && !loading"
      title="Community not found"
      message="This community may no longer be available."
    />
  </PageContainer>

    <PrivateCommunityAccessModal
    v-if="community"
    v-model="showPrivateCommunityModal"
    :community="community"
    :loading="requestLoading"
    :requested="requestSent"
    @request="handlePrivateCommunityRequest"
  />
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityMoreDetails from '~/components/features/community/CommunityMoreDetails.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'
import { useCommunityChat } from '~/composables/useCommunityChat'
import BaseSpinner from '~/components/ui/BaseSpinner.vue'
import PrivateCommunityAccessModal from '~/components/features/community/PrivateCommunityAccessModal.vue'
import { CommunityService } from '~/services/communityService'

const route = useRoute()
const router = useRouter()
const { show } = useSnackBar()


const {
  subToCommNotif,
  unSubToCommNotif
} = useCommunityChat()

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

const showPrivateCommunityModal = ref(false)
const requestLoading = ref(false)
const requestSent = ref(false)

const community = ref(null)
const token = ref('')
const id = ref('')
const showDetails = ref(false)
// const showMembers = ref(false)
// const showEvents = ref(false)

onMounted(async () => {
  const rawToken = localStorage.getItem("access_token")
  if(!rawToken)
      router.push("/auth/signin")
  else{
      token.value = rawToken
      id.value = route.params.id
      community.value = await getCommunityDetails(id.value)

      if (isRestrictedPrivateCommunity.value) {
        requestSent.value = false
        showPrivateCommunityModal.value = true
        return
      }
      if(community.value && community.value.isMember){
        subToCommNotif(id.value, (newMember) => {
          community.value.members.push(newMember)
          community.value.memberCount++
        }) 
      }
  }
 
})

watch(
    () => route.params.id,
    (cId) => {
        if (typeof cId === "string") {
            id.value = cId
            if(community.value?.isMember){
              subToCommNotif(id.value, (newMember) => {
                community.value.members.push(newMember)
                community.value.memberCount++
              }) 
            }
        }
    },
    { immediate: true }
)

const handleJoin = async () => {

  if (isRestrictedPrivateCommunity.value) {
    requestSent.value = false
    showPrivateCommunityModal.value = true
    return
  }

  try {
    const response = await joinCommunity(id.value)

    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember
    subToCommNotif(id.value, (newMember) => {
      community.value.members.push(newMember)
      community.value.memberCount++
    }) 

    show('Nice move! You joined the community.', 'success')
  } catch (err) {
    console.error('Failed to join community.', err)
    show(error.value, 'error')
  }
}

const handlePrivateCommunityRequest = async () => {
  if (!id.value) {
    show('Could not identify this community.', 'error')
    return
  }

  requestLoading.value = true

  try {
    await CommunityService.requestToJoinCommunity(id.value)

    requestSent.value = true
    show(
      'Your request was sent to the community owner.',
      'success'
    )
  } catch (err) {
    console.error('Failed to request community access.', err)

    show(
      err?.data?.message ||
        'Could not send your request. Please try again.',
      'error'
    )
  } finally {
    requestLoading.value = false
  }
}

const handleLeave  = async () => {
  try {
    const response = await leaveCommunity(id.value)

    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember
    unSubToCommNotif()

    show('You left the community.', 'success')
    showDetails.value = false
  } catch (err) {
    console.error('Failed to leave the community.', err)
    show(error.value || 'Could not leave the community.', 'error')
  }
}

const isRestrictedPrivateCommunity = computed(() => {
  if (!community.value) return false

  const isPrivate =
    String(community.value.visibility).toLowerCase() === 'private'

  const hasAccess =
    community.value.isMember === true ||
    community.value.isOwner === true

  return isPrivate && !hasAccess
})

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

    if (isRestrictedPrivateCommunity.value) {
      requestSent.value = false
      showPrivateCommunityModal.value = true
    }
  },
  { immediate: true }
)
</script>

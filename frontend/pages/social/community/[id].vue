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
import BaseButton from '~/components/ui/BaseButton.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'
import { useCommunityChat } from '~/composables/useCommunityChat'
import BaseSpinner from '~/components/ui/BaseSpinner.vue'



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

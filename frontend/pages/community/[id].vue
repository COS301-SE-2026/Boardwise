<template>
  <PageContainer>
    <Navbar />

    <v-container
      v-if="community"
      class="community-detail-page"
    >
      <section
        class="community-chat-window"
        :aria-label="`${community.name} community chat`"
      >
 
      <CommunityBanner 
        :community="community" 
        @members="showMembers = !showMembers"
        @events="showEvents = !showEvents"
        @updated="handleUpdate"
      />
 
      <CommunityChats 
        :community="community" 
        :token="token"
        @join="handleJoin"
      />
    </section>

      <CommunityMoreDetails
        v-model="showDetails"
        :community="community"
        :loading="loading"
        @leave="handleLeave"
      />
    </v-container>

    <output
      v-if="!community && loading"
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

    <BaseEmptyState
      v-if="!community && !loading"
      title="Community not found"
      message="This community may no longer be available."
    />
  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'


const route = useRoute()
const router = useRouter()

const {
  getCommunityDetails,
  joinCommunity,
  leaveCommunity,
  error,
  loading
} = useCommunity()

const { show } = useSnackBar()

const community = ref(null)
const token = ref('')

onMounted(async () => {
  const rawToken = localStorage.getItem("access_token")
  if(!rawToken)
      router.push("/auth/signin")
  else{
      token.value = rawToken;
      community.value = await getCommunityDetails(route.params.id)
  }
 
})

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
</script>
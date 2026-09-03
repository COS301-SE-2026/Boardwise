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
          @details="showDetails = true"
          @updated="handleUpdate"
        />

        <div class="community-chat-window__body">
          <CommunityChats
            :community="community"
            @join="handleJoin"
            @leave="handleLeave"
          />
        </div>
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
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'
import CommunityMoreDetails from '~/components/features/community/CommunityMoreDetails.vue'

const route = useRoute()

const {
  getCommunityDetails,
  joinCommunity,
  leaveCommunity,
  error,
  loading
} = useCommunity()

const { show } = useSnackBar()

const community = ref(null)
const showDetails = ref(false)
const detailsTab = ref('about')

onMounted(async () => {
  community.value = await getCommunityDetails(route.params.id)
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
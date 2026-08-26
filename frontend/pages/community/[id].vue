<template>
   <PageContainer>
    <Navbar />
 
    <div 
      v-if="community" 
      class="d-flex flex-column ga-6 mt-6"
      >
 
      <CommunityBanner 
        :community="community" 
        @members="showMembers = !showMembers"
        @events="showEvents = !showEvents"
        @updated="handleUpdate"
      />
 
      <CommunityChats 
        :community="community" 
        @join="handleJoin"
      />

      <MemberList
        v-model="showMembers"
        :community="community"
      />

      <CommunityEvents
        v-model="showEvents"
        :community="community"
      />
    </div>

    <v-container v-else-if="loading" class="d-flex justify-center align-center" style="min-height: 60vh">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-container>

    <BaseEmptyState
      v-else
      title="Community not found"
    />

  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
 
import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'
import MemberList from '~/components/features/community/MemberList.vue'
import CommunityEvents from '~/components/features/community/CommunityEvents.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'

const route = useRoute()

const {
  getCommunityDetails,
  joinCommunity, 
  error,
  loading
} = useCommunity()

const {
  show
} = useSnackBar()

const showMembers = ref(false)
const showEvents = ref(false)
const community = ref(null)

onMounted(async () => {
  community.value = await getCommunityDetails(route.params.id)
})

const handleJoin = async () => {
  try{
    const response = await joinCommunity(route.params.id)
    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember

    show("Successfully joined community")
  }
  catch(err){
    console.error("Failed to join community.", err)
    show(error.value, 'error')
  }
}

const handleUpdate = (newData) => {
  if(!newData) return

  if(community.value.name != newData.name)
    community.value.name = newData.name

  if(community.value.description !== newData.description)
    community.value.description = newData.description

  if(community.value.visibility !== newData.visibility)
    community.value.visibility = newData.visibility

  if(community.value.imageUrl !== newData.imageUrl)
    community.value.imageUrl = newData.imageUrl

  show("Community details successfully updated")
}


</script>

<template>
   <PageContainer>
    <Navbar />
 
    <div 
      v-if="community" 
      class="d-flex flex-column ga-6 mt-6"
      >
 
      <CommunityBanner 
        :community="community" 
        @members="showMembers = true"
        @events="showEvents = true"
        />
 
      <CommunityChats 
        :community="community" 
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

    <BaseEmptyState
      v-else
      title="Community not found"
    />

  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
 
import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'
import MemberList from '~/components/features/community/MemberList.vue'
import CommunityEvents from '~/components/features/community/CommunityEvents.vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { useCommunity } from '~/composables/useCommunity'

const route = useRoute()

const {
  getCommunityDetails
} = useCommunity()

const showMembers = ref(false)
const showEvents = ref(false)
const community = ref(null)

onMounted(async () => {
  community.value = await getCommunityDetails(route.params.id)
  console.log(community.value)
})

// const community = computed(() =>
//   communities.value.find(
//     (item) => String(item.id) === String(route.params.id)
//   )
// )
</script>

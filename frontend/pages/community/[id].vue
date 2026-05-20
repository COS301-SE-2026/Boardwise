<template>
   <PageContainer>
 
    <Navbar />
 
    <div v-if="community" class="community-page">
 
      <CommunityBanner :community="community" />
 
      <ExploreTabs :active-tab="activeTab" @change="activeTab = $event" />
 
      <CommunityChats v-if="activeTab === 'Chat'" :community="community" />
      <MemberList     v-if="activeTab === 'Members'" :community="community" />
      <CommunityAbout v-if="activeTab === 'About'" :community="community" />
 
    </div>
 
  </PageContainer>
</template>

<script setup>
import { ref } from 'vue'
import { communities } from '~/services/mockData/communities'
 
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
 
import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import ExploreTabs from '~/components/features/community/ExploreTabs.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'
import MemberList from '~/components/features/community/MemberList.vue'
import CommunityAbout from '~/components/features/community/CommunityAbout.vue'

const route = useRoute()

const community = communities.find(
  item => item.id === Number(route.params.id)
)

const activeTab = ref('Chat')

</script>

<style scoped>
.community-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 24px;
}
</style>
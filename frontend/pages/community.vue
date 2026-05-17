<template>
  <PageContainer>
    <Navbar />
    <CommunityHeader />
    <CommunitySearch />
    <CommunityTabs />
    <CommunityGrid />
  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import CommunityHeader from '~/components/features/community/CommunityHeader.vue'
import CommunitySearch from '~/components/features/community/ExploreSearch.vue'
import CommunityTabs from '~/components/features/community/ExploreTabs.vue'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'
</script>
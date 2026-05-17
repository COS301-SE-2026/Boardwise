<template>
  <PageContainer>
    <Navbar />
    <ExploreHeader />
    <PageContainer>
      <CommunityView />
    </PageContainer>
  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import ExploreHeader from '~/components/features/community/ExploreHeader.vue'
import CommunityView from '~/components/features/community/CommunityView.vue'
</script>
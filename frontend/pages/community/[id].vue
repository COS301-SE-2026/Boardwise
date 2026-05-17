<template>
  <PageContainer>

    <Navbar />

    <CommunityView
      v-if="community"
      :community="community"
    />

  </PageContainer>
</template>

<script setup>
import { communities } from '~/services/mockData/communities'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityView from '~/components/features/community/CommunityView.vue'

const route = useRoute()

const community = communities.find(
  item => item.id === Number(route.params.id)
)
</script>
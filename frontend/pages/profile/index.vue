<!-- pages/profile/index.vue -->
<template>
  <PageContainer>

    <Navbar />

    <ProfileHeader
      name="Alexandra Lee"
      username="alexalee"
      bio="Board game lover • Strategy enthusiast"
      avatar="/images/avatar.jpg"
    />

    <ProfileStats
      :games="28"
      :friends="15"
      :communities="8"
      :reviews="12"
    />

    <ProfileTabs
      :active-tab="activeTab"
      @change="activeTab = $event"
    />

    <GamesGrid
      v-if="activeTab === 'Inventory'"
      :games="games"
    />

    <ListingsGrid v-else />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ProfileHeader from '~/components/features/profile/ProfileHeader.vue'
import ProfileStats from '~/components/features/profile/ProfileStats.vue'
import ProfileTabs from '~/components/features/profile/ProfileTabs.vue'

import GamesGrid from '~/components/features/profile/GamesGrid.vue'
import ListingsGrid from '~/components/features/profile/ListingsGrid.vue'

const activeTab = ref('Inventory')

const games = [
  {
    id: 1,
    title: 'Catan',
    category: 'Strategy',
    image: '/images/catan.jpg'
  },
  {
    id: 2,
    title: 'Dixit',
    category: 'Family',
    image: '/images/dixit.jpg'
  }
]
</script>
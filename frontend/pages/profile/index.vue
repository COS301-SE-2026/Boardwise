<template>
  <PageContainer>

    <Navbar />

    <ProfileHeader :user="user" />

    <ProfileStats
      :games="games.length"
      :friends="15"
      :communities="8"
    />

    <ProfileTabs
      :active-tab="activeTab"
      @change="activeTab = $event"
    />

    <GamesOwnedSection
      v-if="activeTab === 'Games Owned'"
      :games="games"
      @add-game="games.push($event)"
    />

    <ListingsSection
      v-else-if="activeTab === 'Listings'"
      :listings="listings"
    />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ProfileHeader from '~/components/features/profile/ProfileHeader.vue'
import ProfileStats from '~/components/features/profile/ProfileStats.vue'
import ProfileTabs from '~/components/features/profile/ProfileTabs.vue'

import GamesOwnedSection from '~/components/features/profile/GamesOwnedSection.vue'
import ListingsSection from '~/components/features/profile/ListingsSection.vue'

const activeTab = ref('Games Owned')

const user = {
  name: 'Alexandra Lee',
  username: 'alexalee',
  bio: 'Board game lover • Strategy enthusiast',
  avatar: '/images/avatar.jpg'
}

const games = ref([
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
  },

  {
    id: 3,
    title: 'Azul',
    category: 'Abstract',
    image: '/images/azul.jpg'
  }
])

const listings = ref([
  {
    id: 1,
    title: 'Catan',
    type: 'sell',
    price: 650,
    negotiable: true,
    location: 'Pretoria',
    image: '/images/catan.jpg'
  },
  {
    id: 2,
    title: 'Dixit',
    type: 'rent',
    price: 400,
    rentalPeriod: '1 week',
    negotiable: false,
    location: 'Pretoria',
    image: '/images/dixit.jpg'
  }
])
</script>
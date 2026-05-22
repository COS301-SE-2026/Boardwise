<template>
  <PageContainer>

    <template v-if="user">
      <Navbar />

      <ProfileHeader :user="user" @saved="user = $event" />

      <ProfileStats
        :games="games.length"
        :friends="15"
        :communities="user.groupCount"
      />

      <ProfileCommunities />

      <v-tabs
        v-model="activeTab"
        color="primary"
        class="mb-4"
      >
        <v-tab value="Games Owned">Games Owned</v-tab>
        <v-tab value="Listings">Listings</v-tab>
      </v-tabs>

      <v-window v-model="activeTab">

        <v-window-item value="Games Owned">
          <GamesOwnedSection
            :games="games"
            @add-game="games.push($event)"
          />
        </v-window-item>

        <v-window-item value="Listings">
          <ListingsSection :listings="listings" />
        </v-window-item>

      </v-window>

    </template>

    <template v-else>
      <v-container class="d-flex justify-center align-center" style="min-height: 60vh">
        <v-progress-circular indeterminate color="primary" size="48" />
      </v-container>
    </template>

  </PageContainer>
</template>

<script setup>
import Navbar             from '~/components/layout/Navbar.vue'
import PageContainer      from '~/components/layout/PageContainer.vue'
import ProfileHeader      from '~/components/features/profile/ProfileHeader.vue'
import ProfileStats       from '~/components/features/profile/ProfileStats.vue'
import ProfileCommunities from '~/components/features/profile/ProfileCommunities.vue'
import GamesOwnedSection  from '~/components/features/profile/GamesOwnedSection.vue'
import ListingsSection    from '~/components/features/profile/ListingsSection.vue'
import { useProfile }     from '~/composables/useProfile'
import { useMarketplace } from '~/composables/useMarketplace'

const { fetchCurrentUser } = useProfile()
const { listings, fetchUserListing, loading, error } = useMarketplace()
const activeTab = ref('Games Owned')
const user      = ref(null)

const defaultGames = [
  { id: 1, title: 'Catan', category: 'Strategy', image: '/images/catan.jpg' },
  { id: 2, title: 'Dixit', category: 'Family',   image: '/images/dixit.jpg' },
  { id: 3, title: 'Azul',  category: 'Abstract', image: '/images/azul.jpg'  }
]

const games = ref([])

const addGame = (game) => {
  games.value.push(game)
  localStorage.setItem('my-games', JSON.stringify(games.value))
}

onMounted(async () => {
  if (!localStorage.getItem('access_token')) router.push('/auth/signin')

  user.value = await fetchCurrentUser()
  await fetchUserListing()

  const stored = localStorage.getItem('my-games')
  // First visit: seed localStorage with defaults, subsequent visits: load saved list
  games.value = stored ? JSON.parse(stored) : defaultGames
  if (!stored) localStorage.setItem('my-games', JSON.stringify(defaultGames))
})
</script>

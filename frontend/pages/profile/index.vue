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
            @add-game="() => { console.log('add-game fired'); showBrowser = true }"
          />
        </v-window-item>

        <v-window-item value="Listings">
          <ListingsSection 
          :listings="listings"
          @deleted="fetchUserListing" 
          @updated="fetchUserListing"

          />
        </v-window-item>

      </v-window>

    </template>

    <template v-else>
      <v-container class="d-flex justify-center align-center" style="min-height: 60vh">
        <v-progress-circular indeterminate color="primary" size="48" />
      </v-container>
    </template>

    <GameBrowserModal
      v-model="showBrowser"
      @confim="handleGamesAdded"
      @add-custom="openCustomModal"
    />

    <AddCustomGameModal
      v-model="showCustom"
      @confirm="handleCustomGame"
      @back-custom="showCustom = false; showBrowser = true"
    />

  </PageContainer>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import { ref, onMounted } from 'vue'
import Navbar             from '~/components/layout/Navbar.vue'
import PageContainer      from '~/components/layout/PageContainer.vue'
import ProfileHeader      from '~/components/features/profile/ProfileHeader.vue'
import ProfileStats       from '~/components/features/profile/ProfileStats.vue'
import ProfileCommunities from '~/components/features/profile/ProfileCommunities.vue'
import GamesOwnedSection  from '~/components/features/profile/GamesOwnedSection.vue'
import ListingsSection    from '~/components/features/profile/ListingsSection.vue'
import GameBrowserModal   from '~/components/features/profile/GameBrowserModal.vue'
import AddCustomGameModal from '~/components/features/profile/AddCustomGameModal.vue'
import { useProfile }     from '~/composables/useProfile'
import { useMarketplace } from '~/composables/useMarketplace'
import { useRouter } from 'vue-router'

const { fetchCurrentUser } = useProfile()
const { listings, fetchUserListing, loading, error } = useMarketplace()
const router = useRouter()
const activeTab = ref('Games Owned')
const user      = ref(null)
const games     = ref([])
const showBrowser = ref(false)
const showCustom  = ref(false)

const defaultGames = [
  { id: 1, title: 'Catan', category: 'Strategy', image: '/images/catan.jpg' },
  { id: 2, title: 'Dixit', category: 'Family',   image: '/images/dixit.jpg' },
  { id: 3, title: 'Azul',  category: 'Abstract', image: '/images/azul.jpg'  }
]

const saveGames = () => {
  localStorage.setItem('my-games', JSON.stringify(games.value))
}

const handleGamesAdded = (selectedGames) => {
  const newGenres = selectedGames.flatMap(g => g.genre ?? [])
  const uniqueGenres = [...new Set(newGenres)]
  console.log('Genres to merge into preferences:', uniqueGenres)

  selectedGames.forEach(game => { 
    if (!games.value.some(g => g.id === game.id)) {
      games.value.push({
        id:       game.id,
        title:    game.title,
        category: game.genre?.[0] ?? '',
        image:    game.imageUrl ?? null  
      })
    }
  })

  saveGames()
}

const openCustomModal = () => {
  showBrowser.value = false
  showCustom.value = true
}

const handleCustomGame = (game) => {
  games.value.push({ ...game, id: Date.now() })
  saveGames()
}

onMounted(async () => {
  const token = localStorage.getItem('access_token')
  if (!token) {
    router.push('/auth/signin')
    return  
  }

  user.value = await fetchCurrentUser()
  await fetchUserListing()

  const stored = localStorage.getItem('my-games')
  games.value = stored ? JSON.parse(stored) : defaultGames
  if (!stored) localStorage.setItem('my-games', JSON.stringify(defaultGames))
})
</script>

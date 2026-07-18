<template>
  <PageContainer>

    <template v-if="user">
      <Navbar />

      <ProfileHeader :user="user" @saved="handleProfileUpdate" />

      <ProfileStats
        :games="user.games.length"
        :friends="user.friendCount"
        :communities="user.groupCount"
      />

      <ProfileCommunities :communities="user.communities" />

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
            @add-game="showBrowser = true"
            @remove-game="handleRemoveGame"
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
      @confirm="handleGamesAdded"
      @add-custom="openCustomModal"
    />

    <AddCustomGameModal
      v-model="showCustom"
      @confirm="handleCustomGame"
      @back="showCustom = false; showBrowser = true"
    />

  </PageContainer>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import { ref, onMounted, computed } from 'vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import ProfileHeader from '~/components/features/profile/ProfileHeader.vue'
import ProfileStats from '~/components/features/profile/ProfileStats.vue'
import ProfileCommunities from '~/components/features/profile/ProfileCommunities.vue'
import GamesOwnedSection from '~/components/features/profile/GamesOwnedSection.vue'
import ListingsSection from '~/components/features/profile/ListingsSection.vue'
import GameBrowserModal from '~/components/features/profile/GameBrowserModal.vue'
import AddCustomGameModal from '~/components/features/profile/AddCustomGameModal.vue'
import { useProfile } from '~/composables/useProfile'
import { useMarketplace } from '~/composables/useMarketplace'
import { useRouter } from 'vue-router'

const { fetchCurrentUser, removeGame } = useProfile();
const { listings, fetchUserListing, loading, error } = useMarketplace();
const router = useRouter();
const activeTab = ref('Games Owned');
const user = ref(null);
const showBrowser = ref(false);
const showCustom = ref(false);
const numGames = ref(0);

const games = computed(()=> user.value?.games??[] );

const refreshUser = async ()=>{
  user.value = await fetchCurrentUser();
  numGames.value = user.value.ownedGameCount;
};

const handleGamesAdded = async () => {
  showBrowser.value = false
  await refreshUser()
}

const openCustomModal = () => {
  showBrowser.value = false
  showCustom.value = true
}

const handleRemoveGame = async(gameId)=>{
  try{
    loading.value = true;
    let response = await removeGame(gameId);
    user.value.ownedGameCount = response.ownedGamesCount;
    user.value.games = response.games;

    // await refreshUser();
  }
  catch(err){
    console.error('Failed to remove game:', err);
  }
  finally{
    loading.value = false;
  }
}

const handleCustomGame = async (response) => {
  showCustom.value = false;

  user.value.ownedGameCount = response.ownedGamesCount;
  user.value.games = response.games;


  // await refreshUser();
}

const handleProfileUpdate = (newValues) => {
  console.log("handle update is indeed called...")
  if(!user.value || !newValues)
    return

  user.value = {
    ...user.value,
    ...newValues
  }

  console.log("handle update is indeed finished with its work...")
}

onMounted(async () => {
  const token = localStorage.getItem('access_token')
  if (!token) {
    router.push('/auth/signin');
    return;
  }

  await refreshUser();
  await fetchUserListing();
});
</script>

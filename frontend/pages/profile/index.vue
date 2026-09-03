<template>
  <PageContainer>

    <!-- Profile loaded -->
    <template v-if="user">
      <Navbar />

      <ProfileHeader :user="user" @saved="handleProfileUpdate" @pfp-change="handlePfpChange"/>

      <ProfileStats
        :games="user.ownedGamesCount"
        :friends="user.friendCount"
        :communities="user.groupCount"
        @open="openFriendsModal"
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
      :games="games"
      @confirm="handleGamesAdded"
      @add-custom="openCustomModal"
    />

    <AddCustomGameModal
      v-model="showCustom"
      @confirm="handleCustomGame"
      @back="showCustom = false; showBrowser = true"
    />

    <FriendsModal
        v-model="showFriendsModal"
        :username="user?.username ?? ''"
        :loading="isLoading"
        @respond="onRespond"
        @remove="handleRemove"
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
import FriendsModal from '~/components/features/people/FriendsModal.vue';

import GamesOwnedSection from '~/components/features/profile/GamesOwnedSection.vue'
import ListingsSection from '~/components/features/profile/ListingsSection.vue'
import GameBrowserModal from '~/components/features/profile/GameBrowserModal.vue'
import AddCustomGameModal from '~/components/features/shared/AddCustomGameModal.vue'
import { useProfile } from '~/composables/useProfile'
import { useSnackBar } from '~/composables/useSnackbar'
import { useMarketplace } from '~/composables/useMarketplace'
import { useFriends } from '~/composables/useFriends'

import { useRouter } from 'vue-router'
import { fa } from 'vuetify/locale'

const { fetchCurrentUser, removeGame } = useProfile();
const { listings, fetchUserListing, loading } = useMarketplace();
const {  isLoading, respondToFriendRequest, unfriendUser } = useFriends()
const { show } = useSnackBar();
const router = useRouter();
const activeTab = ref('Games Owned');
const user = ref(null);
const showBrowser = ref(false);
const showCustom = ref(false);
const numGames = ref(0);
const showFriendsModal = ref(false);

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

const openFriendsModal = async () => {
    if (!user.value) return
    showFriendsModal.value = true
}

const handleRemoveGame = async(gameId)=>{
  try{
    loading.value = true;
    let response = await removeGame(gameId);
    user.value.ownedGameCount = response.ownedGamesCount;
    user.value.games = response.games;

    show("Game successfully removed");
  }
  catch(err){
    console.error('Failed to remove game:', err);
    show("Game removal failed", "error");
  }
  finally{
    loading.value = false;
  }
}

const handleCustomGame = async (response) => {
  showCustom.value = false;
  user.value.ownedGameCount = response.ownedGamesCount;
  user.value.games = response.games;
  show('Game successfully added')
}

const handleProfileUpdate = (newValues) => {
  if(!user.value || !newValues)
    return

  user.value = {
    ...user.value,
    ...newValues
  }
  show("Profile details successfully updated");
}

const handlePfpChange = (newPfp) => {
  if(!newPfp || !user.value)
    return;

  user.value.profilePicture = newPfp.profilePictureUrl;
  show("Profile picture successfully updated");
}

const onRespond = async (id, action) => {
    console.log("respond event emitted and caught")
    try {
        await respondToFriendRequest(id, action)
        await refreshUser()
        await fetchUserListing()
        showFriendsModal.value = false

    } catch (err) {
        console.error('Failed to respond to friend request:', err)
    }
}

const handleRemove = async (id) => {
    if (!user.value) return
    try {
        await unfriendUser(id)
        await refreshUser()
        await fetchUserListing()
        showFriendsModal.value = false
    } catch (err) {
        console.error('Failed to send friend request:', err)
    }
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

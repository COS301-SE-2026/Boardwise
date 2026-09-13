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
            :editable="true"
            @add-game="showBrowser = true"
            @remove-game="handleRemoveGame"
          />
        </v-window-item>

        <v-window-item value="Listings">
          <ListingsSection 
            :listings="listings"
            :editable="true"
            @deleted="fetchUserListing" 
            @updated="fetchUserListing"
          />
        </v-window-item>

      </v-window>

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

      <FriendsModal
          v-model="showFriendsModal"
          :username="user?.username ?? ''"
          :loading="isLoading"
          :friends="userFriendList.friends"
          :mutuals="userFriendList.mutuals"
          @respond="onRespond"
          @remove="handleRemove"
      />
    </template>

    <template v-else>
      <v-container class="d-flex justify-center align-center" style="min-height: 60vh">
        <v-progress-circular indeterminate color="primary" size="48" />
      </v-container>
    </template>

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
import { NotificationType } from "~/services/friendService";

import { useRouter } from 'vue-router'

const { fetchCurrentUser, removeGame } = useProfile();
const { listings, fetchUserListing, loading } = useMarketplace();
const {  isLoading, respondToFriendRequest, unfriendUser, getFriendRequests, getOwnFriendsList, userFriendList, listenForFriendNotifications, userFriendRequests } = useFriends()
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
  show("Your profile changes are locked in.");
}

const handlePfpChange = (newPfp) => {
  if(!newPfp || !user.value)
    return;

  user.value.profilePicture = newPfp.profilePictureUrl;
  show("Looking good! Your profile picture is updated.");
}

const onRespond = async (id, action) => {
    try {
        await respondToFriendRequest(id, action)
        if(action === 'accept'){
          user.value.friendCount++
          const idx = userFriendRequests.value.requests.findIndex((el) => el.id === id);
          const friendRequest = userFriendRequests.value.requests[idx]
          userFriendRequests.value.requests.splice(idx, 1)
          userFriendList.value.friends.push(friendRequest.sender)
        }

    } catch (err) {
        console.error('Failed to respond to friend request:', err)
    }
}

const handleRemove = async (id) => {
    if (!user.value) return
    try {
        await unfriendUser(id)
        user.value.friendCount--
        userFriendList.value = userFriendList.value.friends.filter((el) => el.id !== id)

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

  await fetchUserListing();
  await getFriendRequests();
  await getOwnFriendsList();
  await refreshUser();
  listenForFriendNotifications((notification) => {
      if(notification.type === NotificationType.FRIEND_REQUEST){
          userFriendRequests.value?.requests.push(notification.request);
      }
      else if(notification.type === NotificationType.FRIEND_CONFIRMATION){
          userFriendList.value?.friends.push(notification.friend);
          user.value.friendCount++;
      }
  });
});
</script>

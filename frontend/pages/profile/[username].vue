
<template>
    <PageContainer>

        <template v-if="user">
            <Navbar />

            <div class="d-flex justify-space-between align-center mb-4">
                <div class="d-flex align-center ga-4">
                    <BaseAvatar :src="user.profilePicture ?? undefined" :name="user.username" size="lg" />
                    <div>
                        <h2 class="text-h5 mb-1">@{{ user.username }}</h2>
                        <p v-if="user.bio" class="text-body-2 text-medium-emphasis mb-0">{{ user.bio }}</p>
                    </div>
                </div>

                <FriendActionButton
                    :status="user.FriendStatus"
                    @add="handleAdd"
                    @remove="handleRemove"
                />
            </div>

            <ProfileStats
                :games="user.games.length"
                :friends="user.friendCount"
                :communities="user.groupCount"
                @friends-click="openFriendsModal"
            />

            <ProfileCommunities :communities="user.communities" />

            <v-tabs v-model="activeTab" color="primary" class="mb-4">
                <v-tab value="Games Owned">Games Owned</v-tab>
                <v-tab value="Listings">Listings</v-tab>
            </v-tabs>

            <v-window v-model="activeTab">
                <v-window-item value="Games Owned">
                    <GamesOwnedSection :games="games" />
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

        <FriendsModal
            v-model="showFriendsModal"
            :username="user?.username ?? ''"
            :friends="friends"
            :mutuals="mutuals"
            :loading="friendsLoading"
            @add="onModalAdd"
            @remove="onModalRemove"
        />
    </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue';
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import PageContainer from '~/components/layout/PageContainer.vue';

import ProfileStats from '~/components/features/profile/ProfileStats.vue';
import ProfileCommunities from '~/components/features/profile/ProfileCommunities.vue';
import GamesOwnedSection from '~/components/features/profile/GamesOwnedSection.vue';
import ListingsSection from '~/components/features/profile/ListingsSection.vue';

import FriendsModal from '~/components/features/people/FriendsModal.vue';
import FriendActionButton from '~/components/features/people/FriendActionButton.vue';

import { useProfile } from '~/composables/useProfile'
import { useFriends } from '~/composables/useFriends'
import { useMarketplace } from '~/composables/useMarketplace'
import type { ProfileResponse } from '~/services/userService'

const route = useRoute()
const { fetchUserByUsername } = useProfile()
const { listings, fetchUserListing } = useMarketplace()
const { friends, mutuals, loading: friendsLoading, fetchFriends, sendRequest, removeFriend } = useFriends()


const user = ref<ProfileResponse | null>(null)
const activeTab = ref('Games Owned')
const showFriendsModal = ref(false)

const games = computed(() => user.value?.games ?? [])

const loadProfile = async (username: string) => {
    user.value = await fetchUserByUsername(username) ?? null
    await fetchUserListing(username)
}

const openFriendsModal = async () => {
    if (!user.value) return

    showFriendsModal.value = true
    await fetchFriends(user.value.username)
}

const handleAdd = async () => {
    if (!user.value) return

    try {
        await sendRequest(user.value.username)
        user.value.FriendStatus = 'pendingSent'
    } catch (err) {
        console.error('Failed to send friend request:', err)
    }
}

const handleRemove = async () => {
    if (!user.value) return

    try {
        await removeFriend(user.value.username)
        user.value.FriendStatus = 'none'
    } catch (err) {
        console.error('Failed to remove friend:', err)
  }
}

const onModalAdd = async (username: string) => {
    await sendRequest(username)
    if (user.value) await fetchFriends(user.value.username)
}

const onModalRemove = async (username: string) => {
    await removeFriend(username)
    if (user.value) await fetchFriends(user.value.username)
}

onMounted(() => loadProfile(route.params.username as string))
watch(() => route.params.username, (u) => u && loadProfile(u as string))

</script>
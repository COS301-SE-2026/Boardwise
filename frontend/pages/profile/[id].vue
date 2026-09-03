<template>
    <PageContainer>
        <!-- Loading -->
        <template v-if="loading">
            <v-container
                class="d-flex justify-center align center"
                style="min-height: 60vh"
            >
                <v-progress-circular
                    indeterminate
                    color="primary"
                    size="48"
                />
            </v-container>
        </template>

        <!-- Profile not found -->
        <template v-else-if="notFound">
            <v-container
                class="d-flex justify-center align-center"
                style="min-height: 60vh"
            >
                <BaseEmptyState
                    title="Profile not found"
                    description="The user you're looking for doesn't exist or is no longer available."
                />
            </v-container>
        </template>

        <!-- Profile -->
        <template v-else-if="user">
            <Navbar />

            <div class="d-flex justify-space-between align-center mb-4">
                <div class="d-flex align-center ga-4">
                    <BaseAvatar 
                        :src="user.profilePicture ?? '/images/avatar.jpg'" 
                        :name="user.username" 
                        size="lg" 
                    />

                    <div>
                        <h2 class="text-h5 mb-1"
                            >@{{ user.username }}
                        </h2>

                        <p v-if="user.bio" class="text-body-2 text-medium-emphasis mb-0">{{ user.bio }}</p>
                    </div>
                </div>

                <FriendActionButton
                    :status="user.status"
                    @add="handleAdd"
                    @remove="handleRemove"
                />
            </div>

            <ProfileStats
                :games="user.ownedGameCount"
                :friends="user.friendCount"
                :communities="user.groupCount"
                @open="openFriendsModal"
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

        <FriendsModal
            v-model="showFriendsModal"
            :username="user?.username ?? ''"
            :loading="isLoading"
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
import { FriendStatus } from '~/services/userService';
import type { ProfileResponse } from '~/services/userService'

const route = useRoute()
const { fetchUserById } = useProfile()
const { listings, fetchUserListing } = useMarketplace()
const {  
    isLoading, 
    sendFriendRequest, 
    unfriendUser
} = useFriends()

const loading = ref(true)
const notFound = ref(false)

const user = ref<ProfileResponse | null>(null)
const activeTab = ref('Games Owned')
const showFriendsModal = ref(false)

const games = computed(() => user.value?.games ?? [])

const loadProfile = async (id: string) => {
    isLoading.value = true
    notFound.value = false
    user.value = null

    try {
        const profile = await fetchUserById(id);

        if(!profile) {
            notFound.value = true
            return
        }

        user.value = profile
        await fetchUserListing();
    } catch (err) {
        console.error('Failed to load profile:', err)
        notFound.value = true
    } finally {
        loading.value = false
    }
}

const openFriendsModal = async () => {
    if (!user.value) return

    showFriendsModal.value = true
}

const handleAdd = async () => {
    if (!user.value) return

    try {
        await sendFriendRequest(user.value.id)
        user.value.status = FriendStatus.REQUESTED
    } catch (err) {
        console.error('Failed to send friend request:', err)
    }
}

const handleRemove = async () => {
    if (!user.value) return

    try {
        await unfriendUser(user.value.id)
        user.value.status = FriendStatus.DECLINED
    } catch (err) {
        console.error('Failed to send friend request:', err)
    }
}

const onModalRemove = async (id: string) => {
    try {
        await unfriendUser(id)
        if (user.value) {
            const res = await fetchUserById(user.value.id)
            user.value = res ?? user.value
        }
            
    } catch(err) {
        console.error('Failed to remove friend:', err)
    }
}

onMounted(() => loadProfile(route.params.id as string))
watch(() => route.params.id, (u) => u && loadProfile(u as string))

</script>

<style scoped>

.profile-top {
    flex-wrap: wrap;
    gap: var(--space-4);
}

@media (max-width: 600px) {

    .profile-top {
        align-items: flex-start !important;
    }

}

</style>
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

            <v-card flat class="profile-header pa-10 w-100 mb-6">
                <div class="d-flex justify-space-between align-center flex-wrap ga-6">
                    <div class="d-flex align-center ga-6 flex-wrap profle-info">
                        <BaseAvatar 
                            :src="user.profilePicture ?? '/images/avatar.jpg'" 
                            :name="user.username" 
                            size="lg" 
                        />

                        <div>
                            <h1 class="profile-name ma-0">{{ user.fullName || user.username }}</h1>
                            <p class="profile-username ma-0">@{{ user.username }}</p>
                            
                            <p v-if="user.bio" class="profile-bio ma-0">{{ user.bio }}</p>
                        </div>
                    </div>
                </div>
                <FriendActionButton
                    :status="user.status"
                    @add="handleAdd"
                    @remove="handleRemove"
                />
            </v-card>

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
.profile-header {
    background:    var(--color-surface-alt) !important;
    border-radius: var(--radius-lg) !important;
    border:        1px solid var(--color-border);
    box-shadow:    var(--shadow-sm) !important;
    min-height:    197px; 
}

.profile-avatar {
    border: 3px solid var(--color-border-strong);
    flex-shrink: 0;
}

.profile-info {
    min-width: 0;
    height: auto;
}

.profile-details {
    min-width: 0;
}

.profile-name {
    font-family:  var(--font-display);
    font-size:    var(--fs-h2);
    font-weight:  var(--fw-regular);
    color:        var(--color-secondary);
    line-height:  var(--lh-tight);
}

.profile-username {
    font-family: var(--font-body);
    font-size:   var(--fs-body);
    font-weight: var(--fw-bold);
    color:       var(--color-primary);
}

.profile-bio {
    font-family: var(--font-body);
    font-size:   var(--fs-body);
    color:       var(--color-text-muted);
}

@media (max-width: 600px) {
    .profile-header {
        padding: var(--space-5) !important;
    }

    .profile-info {
        width: 100%;
        align-items: flex-start !important;
    }

    .profile-details {
        flex: 1;
    }

    .profile-name {
        font-size: var(--fs-h3);
    }
}
</style>
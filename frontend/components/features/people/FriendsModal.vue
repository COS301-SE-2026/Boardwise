<template>
    <BaseModal 
        :model-value="modelValue"
        :max-width="420"
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="d-flex justify-space-between align-center mb-4">
            <h3 class="text-h6 mb-0">{{ username }}</h3>
            <BaseButton variant="text" size="small" @click="close"><v-icon>mdi-close</v-icon></BaseButton>
        </div>

        <BaseTabs 
            :tabs="tabs"
            :active-tab="activeTab"
            class="mb-4"
            @change="activeTab = $event"
        />

        <!-- Friends / Mutuals -->
         <template v-if="activeTab !== 'Requests'">
             <BaseSearch v-model="query" placeholder="Search friends..." class="mb-3" />

            <div v-if="loading" class="d-flex flex-column ga-2">
                <BaseSkeleton v-for="n in 3" :key="n" />
            </div>

            <BaseEmptyState
                v-else-if="!visibleList.length"
                :title="activeTab === 'Friends' ? 'No friends yet' : 'No mutual friends'"
            />

            <div v-else class="d-flex flex-column ga-2">
                <div v-for="person in visibleList" :key="person.id" class="d-flex align-center ga-3">
                    <BaseAvatar :src="person.profilePicture ?? undefined" :name="person.username" size="sm" />
                    <span class="flex-grow-1">{{ person.username }}</span>
                    <FriendActionButton
                        status="friends"
                        @remove="$emit('remove', person.username)"
                    />
                </div>
            </div>
         </template>

         <!-- Friend Requests -->
          <template v-else>
            <FriendRequestsList
                :requests="pendingRequests"
                @respond="(requestId, action) => $emit('respond', requestId, action)"
            />
          </template>
    </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseSkeleton from '~/components/ui/BaseSkeleton.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import FriendActionButton from './FriendActionButton.vue'
import FriendRequestsList from './FriendRequestsList.vue'

import type { FriendSummary, FriendRequestSummary } from '~/services/friendService'

const props = defineProps<{
    modelValue: boolean
    username: string
    friends: FriendSummary[]
    mutuals: FriendSummary[]
    pendingRequests: FriendRequestSummary[]
    loading?: boolean
}>()

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'add', username: string): void
    (e: 'remove', username: string): void
    (
        e: 'respond',
        requestId: string,
        action: 'accept' | 'reject'
    ): void
}>()

const tabs = ['Friends', 'Mutuals', 'Requests']
const activeTab = ref<'Friends' | 'Mutuals' | 'Requests'>('Friends')
const query = ref('')

const visibleList = computed(() => {
    const list = activeTab.value === 'Friends' ? props.friends : props.mutuals
    if(!query.value.trim()) return list
    const q = query.value.toLowerCase()
    return list.filter(p => p.username.toLowerCase().includes(q))
})

const close = () => emit('update:modelValue', false)
</script>

<style scoped>
.friend-row {
    min-height: 44px;
}
</style>
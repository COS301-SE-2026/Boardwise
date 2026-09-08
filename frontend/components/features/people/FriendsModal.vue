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
                    <BaseAvatar :src="person.profilePicture ??  '/images/avatar.jpg'" :name="person.username" size="sm" />
                    <span class="flex-grow-1">{{ person.username }}</span>
                    
                    <template v-if="!route.params.id">
                        <BaseButton 
                            @click="handleClick(person.id)" 
                            :variant="'primary'"
                            size="small"
                        >
                            <p>Message</p>
                        </BaseButton>

                        <FriendActionButton
                            :status="FriendStatus.ACCEPTED"
                            @remove="$emit('remove', person.id)"
                        />
                    </template>
                </div>
            </div>
         </template>

         <!-- Friend Requests -->
          <template v-else-if="!route.params.id">
            <FriendRequestsList
                :requests="userFriendRequests!"
                @respond="(requestId, action) => $emit('respond', requestId, action)"
            />
          </template>
    </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseSkeleton from '~/components/ui/BaseSkeleton.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import FriendActionButton from './FriendActionButton.vue'
import FriendRequestsList from './FriendRequestsList.vue'

import type { FriendDTO } from '~/services/friendService'
import { useFriends } from '~/composables/useFriends.ts'
import { useRoute, useRouter } from 'vue-router'
import { FriendStatus } from '~/services/userService.ts'

const {
    userFriendRequests
} = useFriends()

const props = defineProps<{
    modelValue: boolean
    username: string
    loading?: boolean,
    friends: FriendDTO[] | null | undefined,
    mutuals?: FriendDTO[] | null | undefined
}>()

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'add', id: string): void
    (e: 'remove', id: string): void
    (
        e: 'respond',
        requestId: string,
        action: 'accept' | 'decline'
    ): void
}>()

const route = useRoute()
const router = useRouter()

const friends = ref<FriendDTO[]>([])
const mutuals = ref<FriendDTO[]>([])

const tabs = ref<['Friends', 'Requests'] | ['Friends', 'Mutuals']>()
const activeTab = ref<'Friends' | 'Mutuals' | 'Requests'>('Friends')
const query = ref('')

const visibleList = computed(() => {
    const list = activeTab.value === 'Friends' ? friends.value : mutuals.value
    if(!query.value.trim()) return list
    const q = query.value.toLowerCase()
    return list.filter(p => p.username.toLowerCase().includes(q))
})

const close = () => emit('update:modelValue', false)

const handleClick = (id: string) => {
  router.push({
    path: '/chats',
    query: { newChat: id }
  })
}

onMounted(() => {
    if(route.params.id)
        tabs.value = ['Friends', 'Mutuals']
    else
        tabs.value = ['Friends', 'Requests']
})
</script>

<style scoped>
.friend-row {
    min-height: 44px;
}
</style>
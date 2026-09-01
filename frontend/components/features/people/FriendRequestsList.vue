<template>
    <div v-if="requests.length" class="d-flex flex-column ga-2">
        <div v-for="req in requests" :key="req.requestId" class="d-flex align-center ga-3">
            <BaseAvatar :src="req.fromProfilePicture ?? undefined" :name="req.fromUsername" size="sm" />
            <span class="flex-grow-1">{{  req.fromUsername }}</span>
            <BaseButton variant="primary" size="small" @click="respond(req.requestId, 'accept')">Accept</BaseButton>
            <BaseButton variant="ghost" size="small" @click="respond(req.requestId, 'reject')">Reject</BaseButton>
        </div>
    </div>

    <BaseEmptyState v-else title="No pending requests" />
</template>

<script setup lang="ts">
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import { type FriendRequestSummary } from '~/services/friendService'

defineProps<{ requests: FriendRequestSummary[] }>()
const emit = defineEmits<{ (e: 'respond', requestId: string, action:'accept'| 'reject'): void}>()

const respond = (id: string, action: 'accept' | 'reject') => emit('respond', id, action)
</script>
<template>
    <div v-if="requests.requests.length > 0 " class="d-flex flex-column ga-2">
        <div v-for="req in requests.requests" :key="req.id" class="d-flex align-center ga-3">
            <BaseAvatar :src="req.sender.profilePicture ?? undefined" :name="req.sender.fullname" size="sm" />
            <span class="flex-grow-1">{{  req.sender.fullname }}</span>
            <BaseButton variant="primary" size="small" @click="respond(req.id, 'accept')">Accept</BaseButton>
            <BaseButton variant="ghost" size="small" @click="respond(req.id, 'decline')">Reject</BaseButton>
        </div>
    </div>

    <BaseEmptyState v-else title="No pending requests" />
</template>

<script setup lang="ts">
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { type FriendRequestsDTO } from "~/services/friendService";

defineProps<{ requests: FriendRequestsDTO }>()
const emit = defineEmits<{ (e: 'respond', requestId: string, action:'accept'| 'decline'): void}>()

const respond = (id: string, action: 'accept' | 'decline') => emit('respond', id, action)
</script>
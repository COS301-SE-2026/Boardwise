<template>
    <div class="d-flex flex-column ga-4 h-100">

        <template v-if="props.conversation.isInvite">

            <InviteFeed 
                :invites="invites"
                @accept="acceptInvite"
                @decline="declineInvite"
            />
        </template>

        <template v-else>
            <ChatHeader
                :conversation="props.conversation"
            />

            <ChatFeed
                :messages="messages"
                class="flex-grow-1"
            />

            <ChatComposer
                @send="handleSend"
            />
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

import ChatHeader from './ChatHeader.vue';
import ChatFeed from './ChatFeed.vue';
import ChatComposer from './ChatComposer.vue';
import InviteFeed from '../invites/InviteFeed.vue';
import { useSnackBar } from '#imports';
import { getMessages } from '~/services/chatService.js';
import { useEvents } from '~/composables/useEvents';

const {show} = useSnackBar();
const {
    invites,
    fetchInvites,
    respondToInvite
} = useEvents()

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    }
})

const messages = ref<any[]>([])

watch(
    () => props.conversation.id,
    (id: any) => {
        if (!props.conversation.isInvite) {
            messages.value = [...getMessages(id)]
        }
    },
    {
        immediate: true
    }
)

const handleSend = (text: any) => {
    messages.value.push({
        id: Date.now(),
        name: 'You',
        avatar: '/images/avatar.jpg',
        text,
        time: new Date().toLocaleTimeString([],{
            hour: '2-digit',
            minute: '2-digit'
        }),
        isOwn: true
    })
}

const respondingId = ref<string | null>(null)

const acceptInvite = async (eventId: string) => {
    respondingId.value = eventId
    try {
        await respondToInvite(eventId, 'accept')
        show('Invite accepted!', 'success')
    } catch {
        show('Failed to accept invite.', 'error')
    } finally {
        respondingId.value = null
    }
}

const declineInvite = async (eventId: string) => {
try {
        await respondToInvite(eventId, 'decline')
        show('Invite declined', 'info')
    } catch {
        show('Failed to decline invite.', 'error')
    }
}
</script>

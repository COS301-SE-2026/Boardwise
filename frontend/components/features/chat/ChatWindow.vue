<template>
    <div class="chat-window">
        <template v-if="conversation.isInvite">
            <div class="chat-invite-header">
                <BaseButton
                    v-if="showBack"
                    variant="secondary"
                    aria-label="Back to conversations"
                    @click="$emit('back')"
                >
                    <v-icon
                        icon="mdi-arrow-left"
                        class="me-1"
                        aria-hidden="true"
                    />

                    Back
                </BaseButton>

                <div>
                    <h2 class="text-h5 font-weight-bold mb-1">
                        Invites
                    </h2>

                    <p class="text-body-2 text-medium-emphasis mb-0">
                        Review your pending Boardwise event invitations.
                    </p>
                </div>
            </div>

            <InviteFeed
                :invites="invites"
                :responding-id="respondingId"
                @accept="acceptInvite"
                @decline="declineInvite"
            />
        </template>

        <template v-else>
            <ChatHeader
                :conversation="conversation"
                :show-back="showBack"
                @back="$emit('back')"
                @show-details="showUserDetails = true"
            />

            <ChatFeed
                :messages="messages"
                class="flex-grow-1"
            />

            <ChatComposer
                @send="handleSend"
            />

            
        <ChatUserDetails
            v-if="!conversation.isInvite"
            v-model="showUserDetails"
            :conversation="conversation"
                />
        </template>

    </div>
</template>

<script setup lang="ts">
import {
    onMounted,
    ref,
    watch
} from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import ChatUserDetails from './ChatUserDetails.vue'
import ChatComposer from './ChatComposer.vue'
import ChatFeed from './ChatFeed.vue'
import ChatHeader from './ChatHeader.vue'

import InviteFeed from '../invites/InviteFeed.vue'

import { getMessages } from '~/services/chatService.js'
import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '#imports'

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    },

    showBack: {
        type: Boolean,
        default: false
    }
})

defineEmits(['back'])

const showUserDetails = ref(false)

const { show } = useSnackBar()

const {
    invites,
    fetchInvites,
    respondToInvite
} = useEvents()

const messages = ref<any[]>([])
const respondingId = ref<string | null>(null)

onMounted(() => {
    fetchInvites()
})

watch(
    () => props.conversation.id,
    (id: any) => {
        if (props.conversation.isInvite) {
            messages.value = []
            return
        }

        messages.value = [
            ...getMessages(id)
        ]
    },
    {
        immediate: true
    }
)

const handleSend = (text: string) => {
    messages.value.push({
        id: Date.now(),
        name: 'You',
        avatar: '/images/avatar.jpg',
        text,
        time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        }),
        isOwn: true
    })
}

const respondToEventInvite = async (
    eventId: string,
    response: 'accept' | 'decline'
) => {
    respondingId.value = eventId

    try {
        await respondToInvite(
            eventId,
            response
        )

        if (response === 'accept') {
            show('Invite accepted!', 'success')
        } else {
            show('Invite declined.', 'info')
        }
    } catch {
        show(
            `Failed to ${response} invite.`,
            'error'
        )
    } finally {
        respondingId.value = null
    }
}

const acceptInvite = (eventId: string) =>
    respondToEventInvite(eventId, 'accept')

const declineInvite = (eventId: string) =>
    respondToEventInvite(eventId, 'decline')
</script>
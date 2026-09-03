<template>
    <div class="chat-window">

        <template v-if="conversation.isInvite">
            
            <BaseCard class="chat-header pa-4">
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
            </BaseCard>

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
                :conversation="conversation"
                :token="token"
                class="flex-grow-1"
            />

            <ChatComposer
                @send="handleSend"
            />

            <!-- Make this re-direct to their profile for now... -->
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

import { type DirectMessageDTO } from '~/services/chatService.ts'
import { useEvents } from '~/composables/useEvents'
import { usePrivateChat } from '#imports'
import { useSnackBar } from '#imports'
import { jwtDecode } from 'jwt-decode'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    },

    showBack: {
        type: Boolean,
        default: false
    },

    token: {
        type: String,
        required: true
    }
})
defineEmits(['back'])

const showUserDetails = ref(false)

const {
    invites,
    fetchInvites,
    respondToInvite,
} = useEvents();

const { show } = useSnackBar();

const { 
    getMissedMessages,
    sendDirectMessage,
    messages
} = usePrivateChat();

const { 
  onReconnectHook,
} = useStomp();

const respondingId = ref<string | null>(null);

onMounted(() => {
    fetchInvites();
    getMissedMessages(props.conversation.id);
    onReconnectHook(() => getMissedMessages(props.conversation.id))
})

watch(
    () => props.conversation.id,
    async (id: any) => {
        if (props.conversation.isInvite) {
            messages.value = []
            return
        }

        await getMissedMessages(id);
    },
    {
        immediate: true
    }
)

const handleSend = (text: string) => {
    const senderId: string = jwtDecode(props.token).sub ?? "";
    const id = crypto.randomUUID();
    const convoId: string = props.conversation.id;
    const ids: string[] = convoId.split('_');
    const receiverId: string = (ids[0] === senderId ? ids[1] : ids[0]) ?? "";
    const sentAt: string = new Date().toISOString();

    const newMessage: DirectMessageDTO = {
        id,
        senderId,
        receiverId,
        message: text,
        sentAt
    }

    sendDirectMessage(newMessage);
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
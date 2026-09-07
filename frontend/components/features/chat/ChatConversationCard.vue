<template>
    <li class="chat-conversation-list__item">
    <button
        type="button"
        class="chat-conversation-button"
        :class="{
            'chat-conversation-button--active': active,
            'chat-conversation-button--unread': conversation.unread > 0
        }"
        :aria-pressed="active"
        :aria-label="conversationLabel"
        @click="$emit('select', conversation.id)"
    >
        <BaseCard class="chat-conversation-card pa-3">
            <div class="d-flex align-center ga-3">
                <div class="chat-conversation-card__avatar">
                    <BaseAvatar
                        :src="conversation.avatar"
                        :name="conversation.name"
                        size="lg"
                    />

                    <span
                        v-if="conversation.online"
                        class="chat-online-indicator"
                        aria-hidden="true"
                    />

                    <span class="sr-only">
                        {{ conversation.online ? 'Online' : 'Offline' }}
                    </span>
                </div>

                <div class="chat-conversation-card__content">
                    <div class="d-flex justify-space-between align-center ga-3">
                        <span class="chat-conversation-card__name">
                            {{ conversation.name }}
                        </span>

                        <span
                            v-if="conversation.time"
                            class="text-caption text-medium-emphasis"
                        >
                            {{ conversation.time }}
                        </span>
                    </div>

                    <div class="d-flex align-center ga-2 mt-1">
                        <p class="chat-conversation-card__preview">
                            {{ conversation.lastMessage }}
                        </p>

                        <span
                            v-if="conversation.unread"
                            class="chat-unread-count"
                            :aria-label="`${conversation.unread} unread messages`"
                        >
                            {{ conversation.unread }}
                        </span>
                    </div>
                </div>
            </div>
        </BaseCard>
    </button>
    </li>
</template>

<script setup>
import { computed } from 'vue'

import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    },

    active: {
        type: Boolean,
        default: false
    }
})

defineEmits(['select'])

const conversationLabel = computed(() => {
    const unread = props.conversation.unread
        ? `, ${props.conversation.unread} unread`
        : ''

    const status = props.conversation.online
        ? ', online'
        : ', offline'

    return `${props.conversation.name}${status}${unread}`
})
</script>
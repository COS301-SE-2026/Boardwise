<template>
    <article
        class="chat-message"
        :class="{
            'chat-message--own': isOwn
        }"
        :aria-label="messageLabel"
    >
        <BaseAvatar
            v-if="!isOwn"
            :src="conversation.profilePicture"
            :name="conversation.username"
            size="sm"
            class="chat-message__avatar"
        />

        <div class="chat-message__content">
            <span
                v-if="!isOwn"
                class="chat-message__sender"
            >
                {{ conversation.username }}
            </span>

            <div
                class="chat-message__bubble"
                :class="{
                    'chat-message__bubble--own': isOwn
                }"
            >
                <p class="chat-message__text">
                    {{ message.message }}
                </p>

                <span class="chat-message__time">
                    {{ message.sentAt }}
                </span>
            </div>
        </div>

        <BaseAvatar
            v-if="isOwn"
            :src="user.profilePicture"
            :name="user.username"
            size="sm"
            class="chat-message__avatar"
        />
    </article>
</template>

<script setup>
import { computed } from 'vue'


import BaseAvatar from '~/components/ui/BaseAvatar.vue'

const props = defineProps({
    message: {
        type: Object,
        required: true
    },
    conversation: {
        type: Object,
        required: true
    },
    user: {
        type: Object,
        required: true
    }
})


const isOwn = computed(() => {
    return props.message?.senderId === props.user?.id 
    && props.message.senderId === props.user.id;
})

const messageLabel = computed(() => {
    const sender = isOwn.value
        ? 'You'
        : props.conversation.username

    return `${sender}: ${props.message.message}, ${props.message.sentAt}`
})

</script>
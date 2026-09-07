<template>
    <article
        class="chat-message"
        :class="{
            'chat-message--own': message.isOwn
        }"
        :aria-label="messageLabel"
    >
        <BaseAvatar
            v-if="!message.isOwn"
            :src="message.avatar"
            :name="message.name"
            size="sm"
            class="chat-message__avatar"
        />

        <div class="chat-message__content">
            <span
                v-if="!message.isOwn"
                class="chat-message__sender"
            >
                {{ message.name }}
            </span>

            <div
                class="chat-message__bubble"
                :class="{
                    'chat-message__bubble--own': message.isOwn
                }"
            >
                <p class="chat-message__text">
                    {{ message.text }}
                </p>

                <span class="chat-message__time">
                    {{ message.time }}
                </span>
            </div>
        </div>

        <BaseAvatar
            v-if="message.isOwn"
            :src="message.avatar"
            :name="message.name"
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
    }
})

const messageLabel = computed(() => {
    const sender = props.message.isOwn
        ? 'You'
        : props.message.name

    return `${sender}: ${props.message.text}, ${props.message.time}`
})
</script>
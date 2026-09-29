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
            :src="conversation.profilePicture ?? '/images/avatar.jpg'"
            :name="conversation.username"
            size="sm"
            class="chat-message__avatar"
        />

        <div class="chat-message__content">
            <div
                class="chat-message__bubble"
                :class="{
                    'chat-message__bubble--own': isOwn
                }"
            >

                <div v-if="listing">
                    <BaseCard
                        class="chat-message__listing-card pa-3"
                    >
                        <div class="d-flex ga-3 align-center">
                            <BaseImage
                                :src="listing.listingImage"
                                :alt="listing.listingTitle"
                                width="56"
                                height="56"
                                cover
                            />

                            <div class="flex-grow-1">
                                <p class="text-body-2 font-weight-bold mb-0">
                                    {{ listing.listingTitle }}
                                </p>

                                <p class="text-caption text-medium-emphasis mb-0">
                                    R{{ listing.listingPrice }}
                                </p>
                            </div>
                        </div>
                    </BaseCard>
                    <br/>
                    <p class="chat-message__text">
                        I'd like to enquire about this listing
                    </p>
                </div> 

                <p v-else class="chat-message__text">
                    {{ message.message }}
                </p>

                <span class="chat-message__time">
                    {{ formatSentAt(message.sentAt) }}
                </span>
            </div>
        </div>

        <BaseAvatar
            v-if="isOwn"
            :src="user.profilePicture ?? '/images/avatar.jpg'"
            :name="user.username"
            size="sm"
            class="chat-message__avatar"
        />
    </article>
</template>

<script setup>
import { computed } from 'vue'


import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

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

const messagePayload = computed(() => {
    try{
        const parsedMessage = JSON.parse(props.message.message)
        return (parsedMessage && typeof parsedMessage === 'object') ?
                parsedMessage :
                null
    }
    catch{
        return null
    }
})

const listing = computed(() => {
    const result = messagePayload.value?.type === 'LISTING_QUERY' ?
            messagePayload.value :
            null
    if(result)
        console.log("[ChatMessage] listing payload", result)
    return result
})

const isOwn = computed(() => {
    return props.message?.senderId === props.user?.id 
    && props.message.senderId === props.user.id;
})

const formatSentAt = (sentAt) => {
    const date = new Date(sentAt);
    const hours = date.getHours();
    const formattedhours = hours < 10 ? `0${hours}` : hours;

    const minutes = date.getMinutes();
    const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
    
    return `${formattedhours}:${formattedMinutes}`
}

const messageLabel = computed(() => {
    const sender = isOwn.value
        ? 'You'
        : props.conversation.username

    return `${sender}: ${props.message.message}, ${props.message.sentAt}`
})

</script>
<template>
  <article class="chat-message" 
    :class="{ 'chat-message--own': isOwn }"
  >

    <BaseAvatar 
      v-if="!isOwn"
      :src="sender.profilePicture ?? '/images/avatar.jpg'"
      :name="sender.username"
      size="sm"
      class="chat-message__avatar"
    />

    <div class="chat-message__content">

      <div class="d-flex ga-2 align-baseline" 
        :class="{ 'flex-row-reverse': isOwn }"
      >
        <span class="chat-message__sender">{{ sender?.username ?? 'User' }}</span>
        <span class="chat-message__time">{{ formatSentAt(message.sentAt) }}</span>
      </div>

      <div class="chat-message__bubble"
        :class="{ 'chat-message__bubble--own': isOwn }"
      >
        <p class="chat-message__text">
          {{  message.message }}
        </p>
      </div>
    </div>

    <BaseAvatar 
      v-if="isOwn"
      :src="sender.profilePicture ?? '/images/avatar.jpg'"
      :name="sender.username"
      size="sm"
      class="chat-message__avatar"
    />

  </article>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import { computed, watch } from "vue"
import { jwtDecode } from 'jwt-decode'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  token: { 
    type: String, 
    required: true 
  },
  community: {
    type: Object,
    required: true
  }
})

const myUserId = computed(() => {
    try {
        return jwtDecode(props.token).sub
    } catch {
        return null
    }
})

const isOwn = computed(() => {
     return String(props.message?.senderId) === String(myUserId.value)
})

const sender = computed(() => {
    return props.community.members?.find(
        member => String(member.id) === String(props.message?.senderId)
    ) ?? null
})

const formatSentAt = (sentAt) => {
    const date = new Date(sentAt);
    const hours = date.getHours();
    const formattedhours = hours < 10 ? `0${hours}` : hours;

    const minutes = date.getMinutes();
    const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
    
    return `${formattedhours}:${formattedMinutes}`
}
</script>
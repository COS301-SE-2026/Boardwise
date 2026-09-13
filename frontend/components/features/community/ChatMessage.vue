<template>
  <article class="chat-message" 
    :class="{ 'chat-message--own': isOwn }"
  >

    <BaseAvatar 
      v-if="!isOwn"
      :src="sender.profilePicture ?? '/images/avatar.jpg'"
      :name="sender.username"
      size="sm"
      class="chat-message-avatar"
    />

    <div class="chat-message-content">

      <div class="d-flex ga-2 align-baseline" 
        :class="{ 'flex-row-reverse': isOwn }"
      >
        <span class="text-subtitle-2 font-weight-bold">{{ sender?.username ?? 'User' }}</span>
        <span class="text-caption text-medium-emphasis">{{ formatSentAt(message.sentAt) }}</span>
      </div>

      <div class="chat-message-bubble"
        :class="{ 'chat-message-bubble--own': isOwn}"
      >
        <p class="chat-message-text">
          {{  message.message }}
        </p>
      </div>
    </div>

    <BaseAvatar 
      v-if="isOwn"
      :src="sender.profilePicture ?? '/images/avatar.jpg'"
      :name="sender.username"
      size="sm"
      class="chat-message-avatar"
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

// const sender = ref(null)
// const myUserId = ref(jwtDecode(props.token).sub);

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

watch(
  () => props.message.senderId,
  (id) => {
    // if(isOwn.value){
    //   sender.value = props.user
    // }
    sender.value = props.community.members.find((el) => el.id === id);
  },
  { immediate: true }
)


const formatSentAt = (sentAt) => {
    const date = new Date(sentAt);
    const hours = date.getHours();
    const formattedhours = hours < 10 ? `0${hours}` : hours;

    const minutes = date.getMinutes();
    const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
    
    return `${formattedhours}:${formattedMinutes}`
}
</script>
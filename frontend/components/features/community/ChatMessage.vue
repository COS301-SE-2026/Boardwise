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

<style scoped>
.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  margin-bottom: 24px;
  padding: 0 20px;
}

.chat-message--own {
  justify-content: flex-end;
}

.chat-message-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}

.chat-message-content {
  display: flex;
  flex-direction: column;
  max-width: 65%;
  min-width: 0;
}

.chat-message-content > .d-flex {
  margin-bottom: 6px;
}

.chat-message-bubble {
  width: fit-content;
  max-width: 100%;
  padding: 12px 18px;
  border-radius: 10px;
  background-color: white;
  border: 1px solid #e0e0e0;
  box-shadow: none;
}

.chat-message-bubble--own {
  margin-left: auto;
  background-color: #ce2771;
  border-color: #ce2771;
  color: white;
}

.chat-message-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
  word-break: break-word;
}

.chat-message-content .text-subtitle-2 {
  color: #222;
}

.chat-message-content .text-caption {
  font-size: 14px;
}

.chat-message--own .chat-message-content {
  align-items: flex-end;
}

@media (max-width: 600px) {
  .chat-message {
    gap: 10px;
    padding: 0 16px;
    margin-bottom: 20px;
  }

  .chat-message-content {
    max-width: 75%;
  }

  .chat-message-bubble {
    padding: 10px 15px;
  }

  .chat-message-text {
    font-size: 15px;
  }
}

</style>
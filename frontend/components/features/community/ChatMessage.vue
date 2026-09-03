<template>
  <div class="d-flex ga-3 align-start" 
    :class="{ 'flex-row-reverse': isOwn }"
  >

    <BaseAvatar 
      :src="sender.profilePicture ?? '/images/avatar.jpg'"
      :name="sender.username"
      size="md"
    />

    <div class="d-flex flex-column ga-1"
      :class="{ 'align-end': isOwn }"
      style="max-width: 60%;"
    >

      <div class="d-flex ga-2 align-baseline" 
        :class="{ 'flex-row-reverse': isOwn }"
      >

        <span class="text-subtitle-2 font-weight-bold">{{ sender.username }}</span>
        <span class="text-caption text-medium-emphasis">{{ formatSentAt(message.sentAt) }}</span>
      </div>

      <v-sheet
        rounded="lg"
        :border
        class="pa-2 px-4"
        :color="isOwn ? 'primary' : 'surface'"
      >

      <p
        class="d-flex flex-column ga-1"
        :style="{  alignItems: isOwn ? 'flex-end' : 'flex-start' }"
       >
        {{ message.message }}
      </p>
      </v-sheet>

    </div>

  </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import { ref, computed, watch } from "vue"
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

const sender = ref(null)
const myUserId = ref(jwtDecode(props.token).sub);

const isOwn = computed(() => {
    return props.message?.senderId === myUserId.value
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

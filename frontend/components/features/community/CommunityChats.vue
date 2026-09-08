<template>
  <div class="community-chat-window__body">
    <div class="community-chat-content">
      <ChatFeed 
        :messages="messages" 
        :community="community"
        :token="token"
      />

      <div class="community-chat-content__composer">
        <BaseCard 
          v-if="!community.isMember"
          class="pa-4"
        >

          <p class="text-body-2 text-medium-emphasis mb-4">
            Join this community to participate in the discussion
          </p>

          <BaseButton 
            :disabled="loading"
            @click="$emit('join')"
          >
            {{ loading ? 'Joining...' : 'Join community' }}
          </BaseButton>

        </BaseCard>
          
        <ChatInput 
          v-else 
          @send="handleSend"
        />
      </div>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute } from 'vue-router';
import ChatFeed from './ChatFeed.vue';
import ChatInput from './ChatInput.vue';
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseButton from '~/components/ui/BaseButton.vue';
import { useCommunity } from '~/composables/useCommunity.ts';
import { useStomp } from '~/composables/useStomp.ts';
import { useCommunityChat } from '~/composables/useCommunityChat.ts';
import { jwtDecode } from 'jwt-decode';
import { type CommunityMessageDTO } from '~/services/communityService.ts'

const {
  loading
} = useCommunity();

const {
  getMissedCommunityMessages,
  sendGroupMessage,
  messages
} = useCommunityChat();

const { 
  onReconnectHook
} = useStomp();

const route = useRoute();

const props = defineProps({
  community: { type: Object, required: true },
  token: { type: String, required: true }
});

defineEmits(['join'])

onMounted(() => {
  const paramId = route.params.id;
    if(!paramId) return;

    const communityId: string | undefined = Array.isArray(paramId) ? paramId[0] : paramId;
    
    if(communityId){
      getMissedCommunityMessages(communityId);
      onReconnectHook(() => getMissedCommunityMessages(communityId))
    }
})

const handleSend = (text: string) => {
    const paramId = route.params.id;
    if(!paramId) return;

    const communityId: string | undefined = Array.isArray(paramId) ? paramId[0] : paramId;
    if(!communityId) return;

    const senderId: string = jwtDecode(props.token).sub ?? "";
    const id = crypto.randomUUID();
    const sentAt: string = new Date().toISOString();

    const newMessage: CommunityMessageDTO = {
        id,
        senderId,
        communityId,
        message: text,
        sentAt
    }

    sendGroupMessage(newMessage);
}
</script>

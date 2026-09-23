import { computed, onScopeDispose, ref, watch } from 'vue'
import { createSharedComposable } from '@vueuse/core'
import { jwtDecode } from 'jwt-decode'
import { useRoute, useRouter } from 'vue-router'

import { useStomp } from '~/composables/useStomp'
import {
  CommunityService,
  type CommunityMessageDTO,
  type Member
} from '~/services/communityService'

export interface CommunityMessage {
  id: string
  communityId: string
  message: string
}

export interface NewMemberNotification {
  type: 'COMMUNITY_CHAT'
  senderId: string
  message: string
}

const _useCommunityChat = () => {
  const {
    isConnected,
    subscribe,
    unsubscribe,
    sendCommunityMessage
  } = useStomp()

  const route = useRoute()
  const router = useRouter()

  const error = ref('')
  const isLoading = ref(false)
  const messages = ref<CommunityMessageDTO[]>([])
  const activeCommunityId = ref<string | null>(null)

  let chatDestination: string | null = null
  let notificationDestination: string | null = null

  const getToken = () => (
    import.meta.client
      ? localStorage.getItem('access_token')
      : null
  )

  const lastMessageTime = computed(() => {
    if (messages.value.length === 0) {
      return null
    }

    return messages.value[messages.value.length - 1]?.sentAt ?? null
  })

  const sortMessages = () => {
    messages.value.sort(
      (a, b) => (
        new Date(a.sentAt).getTime() -
        new Date(b.sentAt).getTime()
      )
    )
  }

  const upsertMessage = (message: CommunityMessageDTO) => {
    if (
      activeCommunityId.value &&
      String(message.communityId) !== String(activeCommunityId.value)
    ) {
      return
    }

    const existingIndex = messages.value.findIndex(
      existing => String(existing.id) === String(message.id)
    )

    if (existingIndex === -1) {
      messages.value.push(message)
    } else {
      messages.value[existingIndex] = {
        ...messages.value[existingIndex],
        ...message
      }
    }

    sortMessages()
  }

  const getMissedCommunityMessages = async (targetId: string) => {
    error.value = ''
    isLoading.value = true

    const switchingCommunity =
      String(activeCommunityId.value) !== String(targetId)

    if (switchingCommunity) {
      activeCommunityId.value = targetId
      messages.value = []
    }

    const since = switchingCommunity
      ? null
      : lastMessageTime.value

    try {
      const token = getToken()

      if (!token) {
        throw new Error('User is not authenticated')
      }

      const response = await CommunityService.getMissedCommunityMessage(
        targetId,
        since
      )

      response.forEach(upsertMessage)
    } catch (err: any) {
      if (err?.message?.includes('authenticated')) {
        localStorage.removeItem('access_token')
        await router.push('/auth/signin')
        return
      }

      error.value =
        err?.data?.message ||
        'Could not retrieve community messages.'

      throw err
    } finally {
      isLoading.value = false
    }
  }

  const listenForMessages = (id: string) => {
    const token = getToken()

    if (!token) {
      return
    }

    subscribe(
      `/topic/community/${id}/chat`,
      (message: CommunityMessageDTO) => {
        upsertMessage(message)
      }
    )
  }

  const listenForNewMemberJoin = (
    id: string,
    notificationHandler: (member: Member) => void
  ) => {
    if (!getToken()) {
      return
    }

    subscribe(
      `/topic/community/${id}/notification`,
      (notification: NewMemberNotification) => {
        try {
          const newMember = JSON.parse(notification.message) as Member
          notificationHandler(newMember)
        } catch {
          console.error('Invalid community member notification.')
        }
      }
    )
  }

  const sendGroupMessage = (message: CommunityMessageDTO) => {
    if (!getToken()) {
      return
    }

    upsertMessage(message)

    const payload: CommunityMessage = {
      id: message.id,
      communityId: message.communityId,
      message: message.message
    }

    sendCommunityMessage(payload)
  }

  const subscribeToCommunity = (id: string) => {
    if (chatDestination) {
      unsubscribe(chatDestination)
    }

    if (String(activeCommunityId.value) !== String(id)) {
      messages.value = []
    }

    activeCommunityId.value = id
    chatDestination = `/topic/community/${id}/chat`

    listenForMessages(id)
  }

  const subToCommNotif = (
    id: string,
    notificationHandler: (member: Member) => void
  ) => {
    if (notificationDestination) {
      unsubscribe(notificationDestination)
    }

    notificationDestination =
      `/topic/community/${id}/notification`

    listenForNewMemberJoin(id, notificationHandler)
  }

  const unSubToCommNotif = () => {
    if (!notificationDestination) {
      return
    }

    unsubscribe(notificationDestination)
    notificationDestination = null
  }

  watch(
    () => route.params.id,
    id => {
      if (typeof id === 'string') {
        subscribeToCommunity(id)
      }
    },
    { immediate: true }
  )

  onScopeDispose(() => {
    if (chatDestination) {
      unsubscribe(chatDestination)
    }

    if (notificationDestination) {
      unsubscribe(notificationDestination)
    }
  })

  return {
    isConnected,
    isLoading,
    error,
    messages,
    sendGroupMessage,
    getMissedCommunityMessages,
    listenForNewMemberJoin,
    subToCommNotif,
    unSubToCommNotif
  }
}

export const useCommunityChat =
  createSharedComposable(_useCommunityChat)
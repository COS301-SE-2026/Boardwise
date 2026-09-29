<template>
    <BaseCard class="wizard-chat-panel">
        <div class="wizard-chat-panel__header">
            <BaseImage src="/images/Boarley_cute.svg" alt="Boarley" height="32px" width="32px" fit="contain" />

            <div>
                <p class="wizard-chat-panel__title">Boarley Copilot</p>
                <span class="wizard-chat-panel__subtitle">{{  gameTitle  }} Rules</span>
            </div>
        </div>

        <RagFeed :messages="messages" :isLoading="isLoading" :has-no-result="false" @retry="handleRetry" />

        <div v-if="!messages.length" class="wizard-chat-panel__suggestions">
            <span class="wizard-chat-panel__suggestions-label">Quick questions</span>
            <button 
                v-for="q in quickQuestions"
                :key="q"
                type="button"
                class="wizard-chat-panel__suggestion"
                @click="ask(q)"
            >
                {{ q }}
            </button>
        </div>

        <RagComposer :is-loading="isLoading" @send="ask" />
    </BaseCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import RagFeed from '~/components/features/rag/RagFeed.vue'
import RagComposer from '~/components/features/rag/RagComposer.vue'

import type { RagMessage } from '~/composables/useRag'

//Mock RAG until endpoint created

// TODO: Rag backend for setup wizard
// ask() below is mocked

interface LocalMessage {
    id: string
    role: 'user' | 'assistant'
    content: string
    citations?: { chunkId: string; index: number }[]
    isError?: boolean
    query?: string
}

defineProps<{ gameTitle: string }>()

const messages = ref<RagMessage[]>([])
const isLoading = ref(false)

const quickQuestions = ['Do we shuffle resource cards?', 'Can 6 and 8 touch on the board?']

const mockReply = (query: string): string => {
  const q = query.toLowerCase()
  if (q.includes('6') || q.includes('8') || q.includes('touch')) {
    return 'Red numbers (6 and 8) cannot be placed adjacent to one another during board layout.'
  }
  if (q.includes('shuffle') || q.includes('card')) {
    return 'Resource cards stay sorted by type. Only Development cards are shuffled and placed face-down.'
  }
  if (q.includes('robber') || q.includes('desert')) {
    return 'The Robber always starts on the Desert tile. Desert produces no resources.'
  }
  return 'Keep all resource cards in 5 separate face-up bank stacks near the board.'
}

const ask = (query: string) => {
    const trimmed = query.trim()
    if (!trimmed) return

    messages.value.push({ id: crypto.randomUUID(), role: 'user', content: trimmed, query: trimmed })
    isLoading.value = true

    setTimeout(() => {
        messages.value.push({
            id: crypto.randomUUID(),
            role: 'assistant',
            content: mockReply(trimmed),
            citations: [{ chunkId: crypto.randomUUID(), index: 0, content: mockReply(trimmed) }]
        })
        isLoading.value = false
    }, 500)
}

const handleRetry = (message: RagMessage) => {
    if (message.query) ask(message.query)
}
</script>
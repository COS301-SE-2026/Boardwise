<template>
    <BaseCard class="wizard-chat-panel">
        <div class="wizard-chat-panel__header">
            <BaseImage src="/images/Boarley_cute.svg" alt="Boarley" height="32px" width="32px" fit="contain" />

            <div>
                <p class="wizard-chat-panel__title">Boarley Copilot</p>
                <span class="wizard-chat-panel__subtitle">{{ gameTitle }} Rules</span>
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
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import RagFeed from '~/components/features/rag/RagFeed.vue'
import RagComposer from '~/components/features/rag/RagComposer.vue'

import { useRag, type RagMessage } from '~/composables/useRag'

const props = defineProps<{ gameTitle: string; rulebookId: string }>()

const { messages, isLoading, askQuestion } = useRag()

const quickQuestions = ['How do I set up the board?', 'Who goes first?']

const ask = (query: string) => askQuestion(props.rulebookId, query)

const handleRetry = (message: RagMessage) => {
    if (message.query) ask(message.query)
}
</script>
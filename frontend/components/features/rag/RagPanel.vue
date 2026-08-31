<template>
    <v-scale-transition origin="bottom right">
        <v-card 
            v-if="modelValue"
            class="rag-panel"
            elevation="8"
            rounded="lg"
        >
            <div class="rag-panel">
                <div class="rag-header d-flex justify-space-between align-center pa-4">
                    <h3 class="text-subtitle-1 font-weight-bold mb-0">{{  rulebook?.title  }} - Ask AI</h3>
                    <v-btn icon="mdi-close" variant="text" size="small" @click="close" />
                </div> 

                <RagFeed data-test="rag-feed" :messages="messages" :is-loading="isLoading" :has-no-result="false" @retry="handleRetry" />
                <RagComposer data-test="rag-composer" :is-loading="isLoading" @send="handleSend" />
            </div>
        </v-card>
    </v-scale-transition>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import type { RagMessage as RagMessageType } from '~/composables/useRag'

import RagFeed from '~/components/features/rag/RagFeed.vue'
import RagComposer from '~/components/features/rag/RagComposer.vue'

import { useRag } from '~/composables/useRag.ts'

const props = defineProps<{
    modelValue: boolean
    rulebook: { id: string, title: string } | null
}> ()

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
}> ()

const { messages, isLoading, askQuestion, clearConversation } = useRag()

const close = () => emit('update:modelValue', false)

const handleRetry = (message: RagMessageType) => {
    if(!props.rulebook?.id || !message.query) return
    askQuestion(props.rulebook.id, message.query)
}

const handleSend = (query: string) => {
    if (!props.rulebook?.id) return
    askQuestion(props.rulebook.id, query)
}


watch(() => props.rulebook?.id, () => clearConversation())
</script>

<style scoped>
.rag-panel {
    position: fixed;
    right: var(--space-6, 24px);
    bottom: 96px; /* sits just above the floating button */
    width: 380px;
    height: 520px;
    max-height: calc(100vh - 140px);
    display: flex;
    flex-direction: column;
    z-index: 1000;
}

.rag-header {
    border-bottom: 1px solid var(--color-border, #eee);
}

@media (max-width: 600px) {
    .rag-panel {
        right: var(--space-4, 16px);
        left: var(--space-4, 16px);
        width: auto;
        bottom: 88px;
    }
}
</style>
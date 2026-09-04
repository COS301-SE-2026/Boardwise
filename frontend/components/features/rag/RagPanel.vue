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
                    <v-btn icon="mdi-close" variant="text" size="small" aria-label="Close Ask AI panel" @click="close" />
                </div> 

                <RagFeed data-test="rag-feed" :messages="messages" :is-loading="isLoading" :has-no-result="false" @retry="handleRetry" />
                <RagComposer data-test="rag-composer" :is-loading="isLoading" @send="handleSend" />
            </div>
        </v-card>
    </v-scale-transition>
</template>

<script setup lang="ts">
import { watch, onMounted, onUnmounted } from 'vue'
import type { RagMessage as RagMessageType } from '~/composables/useRag'

import RagFeed from '~/components/features/rag/RagFeed.vue'
import RagComposer from '~/components/features/rag/RagComposer.vue'

import { useRag } from '~/composables/useRag'

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

function onKeydown(e: KeyboardEvent) {
    if(e.key === 'Escape' && props.modelValue) close()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

watch(() => props.rulebook?.id, () => clearConversation())
</script>

<style scoped>
.rag-panel {
    position: fixed;
    right: var(--space-6, 24px);
    bottom: 96px; /* sits just above the floating button */
    width: min(380px, calc(100vw - 32px));
    height: min(520px, calc(100vw - 120px));
    max-height: calc(100vh - 140px);
    display: flex;
    flex-direction: column;
    z-index: 1000;
}

.rag-header {
    border-bottom: 1px solid var(--color-border, #eee);
}

@media (max-width: 480px) {
    .rag-panel {
        inset: 0;
        right: auto;
        left: auto;
        width: 100vw;
        bottom: auto;
        height: 100vh;
        max-height: 100vh;
        border-radius: 0;
    }
}
</style>
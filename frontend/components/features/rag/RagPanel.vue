<template>
    <v-scale-transition origin="bottom right">
        <v-card 
            v-if="modelValue"
            class="rag-panel"
            elevation="8"
            rounded="lg"
        >
            <div class="rag-panel__inner">
                <div class="rag-header d-flex justify-space-between align-center pa-4">
                    <h3 class="text-subtitle-1 font-weight-bold mb-0">{{  rulebook?.title  }} - Ask AI</h3>
                    <BaseButton class="rag-close-btn" icon variant="text" size="small" aria-label="Close Ask AI panel" @click="close">
                        <v-icon size="20">mdi-close</v-icon>
                    </BaseButton>
                </div> 

                <RagFeed data-test="rag-feed" :messages="messages" :is-loading="isLoading" :has-no-result="false" :current-user="currentUser" @retry="handleRetry" />
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
    currentUser?: { username?: string; profilePicture?: string } | null
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
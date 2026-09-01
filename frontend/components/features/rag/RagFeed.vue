<template>
    <div class="rag-feed" role="log" aria-live="polite" aria-label="Conversation with Boarley">
        <RagMessage 
            data-test="rag-message"
            v-for="message in messages" 
            :key="message.id" 
            :message="message" 
            @retry="emit('retry', message)"
        />

        <div v-if="isLoading" class="rag-message assistant">
            <v-progress-circular data-test="v-progress-circular" size="20" color="primary" />
        </div>

        <div v-if="!messages.length && !isLoading" class="rag-empty-state">
            Ask a question about this rulebook - answers are grounded in its actual text.
        </div>

        <div v-else-if="hasNoResult" data-test="rag-no-result" class="text-body-2 text-medium-emphasis rag-no-result">
            I could not find anything relevant to that in this rulebook. Try rephrasing, or check you've selected the right game.
        </div>
    </div>
</template>

<script setup lang="ts">
import RagMessage from './RagMessage.vue'
import type { RagMessage as RagMessageType } from '~/composables/useRag'

defineProps<{
    messages: RagMessageType[]
    isLoading?: boolean
    hasNoResult?: boolean
}>()

const emit = defineEmits<{
    (e: 'retry', message: RagMessageType): void
}>()
</script>

<style scoped>
.rag-feed {
    flex-grow: 1;
    overflow-y: auto;
    padding: var(--space-4, 16px);
}

.rag-empty-state {
    font-size: var(--fs-body, 15px);
    color: var(--color-text-muted);
    line-height: var(--lh-normal, 1.5);
}
</style>
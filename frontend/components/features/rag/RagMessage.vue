<template> 
    <div class="rag-message" :class="message.role" data-test="rag-message">
        <BaseCard :class="['rag-bubble', { 'rag-bubble-error': message.isError }]">
            <p class="mb-0">{{  message.content  }}</p>
            <RagCitation
                v-for="citation in message.citations"
                :key="citation.chunkId"
                :citation="citation"
            />
        </BaseCard>
    </div>
</template>

<script setup lang="ts">
import BaseCard from '~/components/ui/BaseCard.vue'
import RagCitation from './RagCitation.vue'
import type { RagMessage } from '~/composables/useRag'

defineProps<{message: RagMessage }>()
</script>

<style scoped>
.rag-message { 
    display: flex;
    margin-bottom: var(--space-3, 12px); 
}

.rag-message.user { 
    justify-content: flex-end; 
}

.rag-message.assistant { 
    justify-content: flex-start; 
}

.rag-bubble { 
    max-width: 80%; 
    padding: var(--space-3, 12px) var(--space-4, 16px); }

.rag-bubble-error { 
    border: 1px solid rgb(var(--v-theme-error)); 
}

</style>
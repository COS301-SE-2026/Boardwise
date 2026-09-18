<template> 
    <div class="rag-message" :class="message.role" data-test="rag-message">
        <BaseAvatar 
            v-if="message.role === 'assistant'"
            src="/images/Boarley_cute.svg"
            alt="Boarley"
            size="sm"
            class="rag-avatar"
        />

        <BaseCard :class="['rag-bubble', { 'rag-bubble-error': message.isError }]">
            <p class="mb-0">{{  message.content  }}</p>
            <RagCitation
                v-for="citation in message.citations"
                :key="citation.chunkId"
                :citation="citation"
            />

            <BaseButton v-if="message.isError" size="small" variant="ghost" class="mt-2 rag-retry-btn" @click="$emit('retry', message)">
                Retry
            </BaseButton>
        </BaseCard>
    </div>
</template>

<script setup lang="ts">
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import RagCitation from './RagCitation.vue'
import type { RagMessage } from '~/composables/useRag'

defineProps<{message: RagMessage }>()

defineEmits<{ (e: 'retry', message: RagMessage ): void }>()
</script>
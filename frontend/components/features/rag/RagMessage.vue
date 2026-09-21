<template> 
    <div class="rag-message" :class="`rag-message--${message.role}`" data-test="rag-message">
        <BaseAvatar 
            v-if="message.role === 'assistant'"
            src="/images/Boarley_cute.svg"
            alt="Boarley"
            size="sm"
            class="rag-avatar"
        />

        <BaseCard :class="['rag-bubble', { 'rag-bubble-error': message.isError }]">
            <p class="mb-0">{{  message.content  }}</p>

            <div v-if="message.citations?.length" class="rag-citations">
                <RagCitation
                    v-for="citation in message.citations"
                    :key="citation.chunkId"
                    :citation="citation"
                />
            </div>

            <BaseButton v-if="message.isError" size="small" variant="ghost" class="mt-2 rag-retry-btn" @click="$emit('retry', message)">
                Retry
            </BaseButton>
        </BaseCard>

        <BaseAvatar
            v-if="message.role === 'user'"
            :src="currentUser?.profilePicture ?? '/images/avatar.jpg'"
            :name="currentUser?.username"
            size="sm"
            class="rag-avatar"
        />
    </div>
</template>

<script setup lang="ts">
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import RagCitation from './RagCitation.vue'

import type { RagMessage } from '~/composables/useRag'

defineProps<{message: RagMessage ,
    currentUser?: { username?: string; profilePicture?: string } | null
}>()

defineEmits<{ (e: 'retry', message: RagMessage ): void }>()
</script>
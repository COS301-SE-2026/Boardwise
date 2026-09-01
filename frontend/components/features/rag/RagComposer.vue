<template>
    <BaseCard class="pa-4">
        <div class="d-flex ga-3">
            <BaseInput
                v-model="text"
                aria-label="Ask a question about this rulebook"
                class="flex-grow-1"
                placeholder="Ask a question about this rulebook..."
                :disabled="isLoading"
                maxlength="500"
                @keyup.enter="send"
            />

            <BaseButton :disabled="isLoading || !text.trim()" @click="send">
                Send
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue';
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseInput from '~/components/ui/BaseInput.vue';

defineProps<{
    isLoading?: boolean
}>() 

const emit = defineEmits<{
    (e: 'send', message: string): void
}> ()

const text = ref('')

const send = () => {
    const message = text.value.trim()
    if (!message) return
    emit('send', message)
    text.value = ''
}
</script>

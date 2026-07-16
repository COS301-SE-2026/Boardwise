<template>
    <BaseCard class="pa-4">
        
        <div class="d-flex ga-3">

            <BaseInput
                v-model="text"
                class="flex-grow-1"
                placeholder="Type a message..."
                @keyup.enter="send"
            />

            <BaseButton
                :disabled="!text.trim()"
                @click="send"
            >
                Send
            </BaseButton>
        </div>
    </BaseCard>
</template>
<script setup>
import { ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue';
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseInput from '~/components/ui/BaseInput.vue';

const emit = defineEmits(['send'])

const text = ref('')

const send = () => {
    const message = text.value.trim()

    if(!message) return

    emit('send', message)
    text.value = ''
}
</script>

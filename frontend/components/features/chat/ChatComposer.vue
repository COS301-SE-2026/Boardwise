<template>
    <BaseCard class="chat-composer-card pa-4">
        <form
            class="chat-composer"
            aria-label="Send a message"
            @submit.prevent="send"
        >
            <BaseInput
                v-model="text"
                class="chat-composer__input"
                placeholder="Type a message..."
                aria-label="Message"
                name="message"
                autocomplete="off"
            />

            <BaseButton
                type="submit"
                :disabled="!canSend"
            >
                <v-icon
                    icon="mdi-send"
                    size="18"
                    class="me-2"
                    aria-hidden="true"
                />

                Send
            </BaseButton>
        </form>
    </BaseCard>
</template>

<script setup>
import { computed, ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

const emit = defineEmits(['send'])

const text = ref('')

const canSend = computed(() =>
    text.value.trim().length > 0
)

const send = () => {
    const message = text.value.trim()

    if (!message) return

    emit('send', message)

    text.value = ''
}
</script>
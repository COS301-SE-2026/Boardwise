<template>
    <BaseCard>
        <p class="card-title">Table Floor &amp; Announcements</p>

        <div class="table-chat-feed">
            <div v-for="m in messages" :key="m.id" class="table-chat-message" :class="{ 'table-chat-message--host': m.isHost }">
                <BaseAvatar :name="m.user" size="sm" />
                <div class="table-chat-message__body">
                    <div class="table-chat-message__meta">
                        <span class="card-subtitle" style="margin: 0">@{{  m.user }}</span>
                        <BaseBadge v-if="m.isHost" variant="primary" size="x-small">Host</BaseBadge>
                        <span class="card-meta">{{ formatTime(m.ts) }}</span>
                    </div>
                    <p style="margin: 0">{{ m.text }}</p>
                </div>
            </div>

            <BaseEmptyState v-if="!messages.length" size="compact" title="No messages yet" message="Say hello to the table." />
        </div>

        <form class="table-chat-composer" @submit.prevent="handleSend">
            <BaseInput v-model="draft" placeholder="Post a table note..." hide-details />
            <BaseButton icon type="submit" :disabled="!draft.trim()"><v-icon>mdi-send</v-icon></BaseButton>
        </form>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({ messages: { type: Array, default: () => [] } })
const emit = defineEmits(['send'])

const draft = ref('')
const handleSend = () => {
    if(!draft.value.trim()) return
    emit('send', draft.value)
    draft.value = ''
}

const formatTime = (ts) => new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
</script>
<template>
    <BaseCard class="chat-header pa-4">
        <div class="d-flex align-center ga-3">
            <BaseButton
                v-if="showBack"
                variant="secondary"
                class="chat-header__back"
                aria-label="Back to conversations"
                @click="$emit('back')"
            >
                <v-icon
                    icon="mdi-arrow-left"
                    class="me-1"
                    aria-hidden="true"
                />

                Back
            </BaseButton>

            <button
                type="button"
                class="chat-header__identity"
                :aria-label="`View details about ${conversation.username}`"
                @click="$emit('show-details')"
            >
                <div class="chat-header__avatar">
                    <BaseAvatar
                        :src="conversation.profilePicture "
                        :name="conversation.username + ' profile picture'"
                        size="lg"
                    />
                </div>

                <div class="flex-grow-1 overflow-hidden">
                    <h2 class="chat-header__name">
                        {{ conversation.username }}
                    </h2>

                <div class="chat-header__status">
                    <span
                        :class="{
                            'chat-status-dot--online': conversation.online
                        }"
                        aria-hidden="true"
                    />

                        <span>
                            {{ conversation.isOnline ? 'Online' : 'Offline' }}
                        </span>
                    </div>
                </div>

                <v-icon
                    icon="mdi-chevron-right"
                    class="chat-header__details-icon"
                    aria-hidden="true"
                />
            </button>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
    conversation: {
        type: Object,
        required: true
    },

    showBack: {
        type: Boolean,
        default: false
    }
})

defineEmits(['back', 'show-details'])
</script>
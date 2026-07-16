<template>
    <BaseCard
        class="pa-3 cursor-pointer"
        :class="{ 'border border-primary': active }"
        @click="$emit('select', props.conversation.id)"
    >

        <div class="d-flex align-center ga-3">
            <div class="position-relative">
                <BaseAvatar
                    :src="props.conversation.avatar"
                    :name="props.conversation.name"
                    size="lg"
                />

                <span
                    v-if="props.conversation.online"
                    class="online-indicator rounded-circle"
                />
            </div>

            <div class="flex-grow-1 overflow-hidden">
                <div class="d-flex justify-space-between align-center">
                    <h4 class="text-body-1 mb-0">
                        {{ props.conversation.name }}
                    </h4>

                    <span class="text-caption text-medium-emphasis">
                        {{ props.conversation.time }}
                    </span>
                </div>
                <p class="text-body-2 text-medium-emphasis text-truncate mb-0">
                    {{ props.conversation.lastMessage }}
                </p>
            </div>

            <v-badge
                v-if="props.conversation.unread"
                :content="props.conversation.unread"
                color="primary"
                inline
            />
        </div>
    </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import BaseCard from '~/components/ui/BaseCard.vue';


const props = defineProps({
    conversation: {
        type: Object,
        required: true
    },
    active: {
        type: Boolean,
        default: false
    }
})

defineEmits(['select'])

</script>

<style scoped>
.online-indicator{
    position: absolute;
    right: 0;
    bottom: 0;
    width: 12px;
    height: 12px;
    background: var(--color-success);
    border: 2px solid var(--color-surface);
}
</style>
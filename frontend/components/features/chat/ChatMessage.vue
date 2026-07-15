<template>
    <div
        class="d-flex ga-3 mb-5"
        :class="{ 'justify-end': message.isOwn }"
    >
        <BaseAvatar
            v-if="!message.isOwn"
            :src="message.avatar"
            :name="message.name"
            size="sm"
        />

        <div class="bubble pa-3 rounded-lg"
            :class="{ own: message.isOwn }">
            <p class="mb-0">
                {{ message.text }}
            </p>
            <span class="time text-caption d-block mt-2">
                {{  message.time }}
            </span>
        </div>

        <BaseAvatar
            v-if="message.isOwn"
            :src="message.avatar"
            :name="message.name"
            size="sm"
        />
    </div>
</template>   

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue';

defineProps({
    message: {
        type: Object,
        required: true
    }
})
</script>

<style scoped>
.bubble{
    max-width: 420px;
    background: var(--color-surface);
}

.own .bubble{
    background: var(--color-primary);
    color: var(--color-text-inverse);
}

.time{
    opacity: 0.7;
}
</style>
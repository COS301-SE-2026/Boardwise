<template>
    <div class="boarley-bubble">
        <BaseAvatar v-if="showAvatar" src="/images/Boarley.svg" alt="Boarley" size="lg" />
        <div class="boarley-bubble_text">
            <slot />
        </div>
    </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

defineProps({
    showAvatar: {
        type: Boolean,
        default: true
    }
})
</script>
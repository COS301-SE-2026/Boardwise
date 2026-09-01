<template> 
    <v-btn 
        data-test="ai-floating-button"
        class="ai-fab"
        color="primary"
        icon
        size="56"
        elevation="4"
        aria-label="Ask Boarley about this rulebook"
        @click="$emit('click')"
    >
       <BaseAvatar src="/images/Boarley_cute.svg" alt="Boarley" size="sm" />
    </v-btn>
</template>

<script setup>
import BaseAvatar from '../ui/BaseAvatar.vue';

defineEmits(['click'])
</script>

<style scoped>
.ai-fab{
    position: fixed;
    right: var(--space-6, 24px);
    bottom: var(--space-6, 24px);
    z-index: 1000;
}

@media (max-width: 480px) {
    .ai-fab {
        right: var(--space-4, 16px);
        bottom: var(--space-4, 16px);
    }
}
</style>
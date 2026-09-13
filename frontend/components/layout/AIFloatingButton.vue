<template> 
    <BaseButton 
        data-test="ai-floating-button"
        class="ai-fab"
        icon
        size="56"
        aria-label="Ask Boarley about this rulebook"
        @click="$emit('click')"
    >
       <BaseAvatar src="/images/Boarley_cute.svg" alt="Boarley" size="sm" />
    </BaseButton>
</template>

<script setup>
import BaseAvatar from '../ui/BaseAvatar.vue'
import BaseButton from '../ui/BaseButton.vue';

defineEmits(['click'])
</script>

<style scoped>
.ai-fab{
    position: fixed;
    right: var(--space-6, 24px);
    bottom: var(--space-6, 24px);
    z-index: 1000;

    box-shadow: var(--shadow-lg) !important;
}

@media (max-width: 480px) {
    .ai-fab {
        right: var(--space-4, 16px);
        bottom: var(--space-4, 16px);
    }
}
</style>
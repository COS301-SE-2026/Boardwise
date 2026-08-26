<template> 
    <v-btn 
        data-test="ai-floating-button"
        class="ai-fab"
        color="primary"
        icon
        size="56"
        elevation="4"
        @click="$emit('click')"
    >
        <v-icon size="28">mdi-robot</v-icon>
    </v-btn>
</template>

<script setup>
defineEmits(['click'])
</script>

<style scoped>
.ai-fab{
    position: fixed;
    right: var(--space-6, 24px);
    bottom: var(--space-6, 24px);
    z-index: 1000;
}

@media (max-width: 970px) {
    .ai-fab {
        right: var(--space-4, 16px);
        bottom: var(--space-4, 16px);
    }
}
</style>
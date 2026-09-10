<!-- Base Card for every other Card (Marketplace, Library and Profile Cards) -->
<template> 
    <v-card 
        rounded="lg"
        :elevation="0"
        border
        class="base-card"
        v-bind="$attrs"
    >
        <slot />
    </v-card>
</template>

<style scoped>
.base-card {
    overflow: hidden;
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);

    transition: 
        transform var(--transition-base),
        box-shadow var(--transition-base),
        border-color var(--transition-base);
}

@media (hover: hover) and (pointer: fine) {
    .base-card:hover {
        transform: translateY(-2px);
        border-color:var(--color-primary);
        box-shadow: var(--shadow-md) !important;
    }
}

@media (prefers-reduced-motion: reduce) {
    .base-card:hover {
        transform: none;
    }
}
</style>
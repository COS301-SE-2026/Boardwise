<template>
    <div class="boarley-bubble">
        <BaseAvatar src="/images/Boarley.svg" alt="Boarley" size="xl" />
        <div class="boarley-bubble_text">
            <slot />
        </div>
    </div>
</template>

<style scoped>
.boarley-bubble {
    display: flex;
    align-items: flex-start;
    gap: var(--space-3);
}

.boarley-bubble_text {
    background: var(--color-surface-alt);
    border-radius: var(--radius-md);
    padding: var(--space-3) var(--space-4);
    font-family: var(--font-body);
    font-size: var(--fs-body);
    color: var(--color-text);
}
</style>
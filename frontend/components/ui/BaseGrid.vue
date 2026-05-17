<template> 
    <div class="grid">
        <slot />
    </div>
</template>

<style scoped> 
.grid {
    display: flex;
    grid-template-columns: 
        repeat(auto-fit, minmax(220, 1fr));
    gap: 24px;
}
</style>
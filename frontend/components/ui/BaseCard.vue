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
    transition: 
        transform var(--transition-base),
        box-shadow var(--transition-base);
}

.base-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md) !important;
}
</style>
<template>
    <v-sheet
        variant="flat"
        width="260"
        style="flex-shrink: 0"
        class="d-flex flex-column ga-2 pa-4 base-filter-sidebar"
    >
        <h3 class="base-filter-sidebar__heading">Filters</h3>
        <slot />
        <BaseButton variant="secondary" @click="$emit('reset')">↺ Reset</BaseButton>
    </v-sheet>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
defineEmits(['reset'])
</script>

<style scoped>
.base-filter-sidebar {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
}
</style>
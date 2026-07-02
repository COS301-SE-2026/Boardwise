<template>
    <v-sheet
        rounded="lg"
        border
        width="260"
        style="flex-shrink: 0"
        class="d-flex flex-column ga-2 pa-4"
    >
        <h3>Filters</h3>
        <slot />
        <BaseButton variant="secondary" @click="$emit('reset')">↺ Reset</BaseButton>
    </v-sheet>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
defineEmits(['reset'])
</script>
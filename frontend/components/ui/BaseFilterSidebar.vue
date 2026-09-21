<template>
    <v-sheet
        variant="flat"
        width="260"
        style="flex-shrink: 0"
        class="d-flex flex-column ga-2 pa-4 base-filter-sidebar"
    >
        <div class="d-flex justify-space-between align-center mb-2">
            <h2 class="base-filter-sidebar__heading">Filters</h2>
            <BaseButton 
                variant="text" 
                size="sm"
                class="base-filter-sidebar__reset"
                @click="$emit('reset')"
            >
                ↺ Reset
            </BaseButton>
        </div>
        
        <slot />
        
    </v-sheet>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
defineEmits(['reset'])
</script>
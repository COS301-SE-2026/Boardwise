<template>
    <v-sheet
        rounded="lg"
        border
        width="260"
        style="flex-shrink: 0"
        class="base-filter-sidebar d-flex flex-column ga-2 pa-4"
        role="region"
        :aria-labelledby="headingId"
    >
        <h3 :id="headingId" class="base-filter-sidebar__heading">Filters</h3>
        <slot />
        <BaseButton variant="secondary" aria-label="Reset filters" @click="$emit('reset')"><span aria-hidden="true">↺</span> Reset</BaseButton>
    </v-sheet>
</template>

<script setup>
import { useId } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
    title: {
        type: String,
        default: 'Filters'
    }
})
defineEmits(['reset'])
const headingId = useId()
</script>
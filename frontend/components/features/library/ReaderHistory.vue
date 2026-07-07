<template>
    <v-navigation-drawer
        :model-value="modelValue"
        location="right"
        temporary
        width="420"
        scrollable
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="pa-6">
            <div class="d-flex justify-space-between align-center mb-6">
                <h2>Edit History</h2>
                <v-btn icon variant="text" @click="$emit('update:modelValue', false)">
                <v-icon>mdi-close</v-icon>
                </v-btn>
            </div>

            <slot />
        </div>
    </v-navigation-drawer>
</template>

<script setup>
defineProps({
  modelValue: { 
    type: Boolean, 
    default: false 
  }   
})

defineEmits(['update:modelValue'])
</script>
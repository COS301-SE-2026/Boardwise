<template>
    <div class="tabs">
        <BaseButton 
            v-for="tab in tabs"
            :key="tab"
            :class="{ active: tab === activeTab }"
            @click="$emit('change', tab)"
        >
            {{ tag }}
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from './BaseButton.vue'

defineProps({
    tabs: Array,
    activeTab: String
})

defineEmits(['change'])
</script>

<style scoped> 
.tabs {
    display: flex;
    gap: 12px;
}

.active {
    background: #6C3BFF;
    color: white;
}
</style>
<template>
    <div class="tabs">
        <div 
            v-for="tab in tabs"
            :key="tab"
            class="tab"
            :class="{ active: tab === activeTab }"
            @click="$emit('change', tab)"
        >
            {{ tab }}
        </div>
    </div>
</template>

<script setup>
defineProps({
    tabs: Array,
    activeTab: String
})

defineEmits(['change'])
</script>

<style scoped> 
.tabs {
    display: flex;
    gap: 32px;
    border-bottom: 1px solid #E5E7EB;
    margin-top: 32px;
}

.tab {
    position: relative;
    padding-bottom: 14px;
    cursor: pointer;
    font-weight: 600;
    color: #777;
    transition: 0.2s ease;
}

.tab:hover {
    color: #111;
}

.active {
    background: #6C3BFF;
    color: white;
}

.active::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: -1px;
    width: 100%;
    height: 3px;
    background: #6C3BFF;
    border-radius: 999px;
}
</style>
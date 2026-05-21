<template>
    <v-tabs
        :model-value="activeTab"
        @update:model-value="$emit('change', $event)"
    >
        <v-tab
            v-for="tab in tabs"
            :key="tab"
            :value="tab"
        >
            {{ tab }}
        </v-tab>    
    </v-tabs>
</template>

<script setup>
defineProps({
    tabs: Array,
    activeTab: String
})

defineEmits(['change'])
</script>
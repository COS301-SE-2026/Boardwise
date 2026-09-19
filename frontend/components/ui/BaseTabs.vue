<template>
    <v-tabs
        :model-value="activeTab"
        :aria-label="ariaLabel"
        class="base-tabs"
        @update:model-value="$emit('change', $event)"
    >
        <v-tab
            v-for="tab in tabs"
            :key="tab"
            :value="tab"
        >
            <slot :name="`tab-${tab}`">
                {{ tab }}
            </slot>
        </v-tab>    
    </v-tabs>
</template>

<script setup>
defineProps({
    tabs: {
      type: Array,
      default: () => []
    },
    activeTab: {
      type: String,
      default: ''
    },
    ariaLabel: {
      type: String,
      default: 'Page sections'
    }
})

defineEmits(['change'])
</script>
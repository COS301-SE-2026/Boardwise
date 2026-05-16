<template> 
    <div class="tabs">

        <BaseButton 
            v-for="tab in tabs"
            :key="tab"

            :class="{ active: selectedTab === tab}"
            
            @click="selectedTab = tab"
        />
            {{ tab }}

        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const tabs = [
    {
        'All',
        'Strategy',
        'Family',
        'Party'
    },
]

const selectedTab = ref('All')
</script>

<style scoped>
.tabs {
    display: flex;
    gap: 12px;
    overflow-x: auto;
}

.active {
    background: #7B2CBF;
    color: white;
}
</style>
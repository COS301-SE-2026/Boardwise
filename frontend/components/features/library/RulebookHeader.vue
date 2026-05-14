<template> 
    <div class="header">

        <SectionTitle 
            title="Library"
            subtitle="Browse rulebooks and guides"
        />

        <BaseInput placeholder="Search rulebooks..."
        />

        <RulebookTabs />

    </div>
</template>

<script setup>
import SectionTitle from './components/ui/SectionTitle.vue'
import BaseInput from './components/ui/BaseInput.vue'

import RulebookTabs from './RulebookTabs.vue'
</script>

<style scoped>
.header {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
</style>
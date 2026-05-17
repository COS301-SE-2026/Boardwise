<template>
    <div class="header">

        <SectionTitle
            title="Commmunity"
            subtitle="Join a community"
        />

        <!-- <BaseInput 
            placeholder="Search games..."
        />

        <MarketplaceTabs /> -->

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
</script>

<style scoped>
.header {
    display: flex;
    flex-direction: column;
    gap: 20px;
    
}
</style>
<template>
    <div class="header">

        <SectionTitle
            title="Marketplace"
            subtitle="Buy, rent and sell games"
        />

        <!-- <BaseInput 
            placeholder="Search games..."
        />

        <MarketplaceTabs /> -->

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import MarketplaceTabs from './MarketplaceTabs.vue'
</script>

<style scoped>
.header {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
</style>
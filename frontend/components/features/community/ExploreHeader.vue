<template>
    <div class="header">

        <SectionTitle title="Community" subtitle="Find your people" />
        <BaseInput placeholder="Explore communities " />
        <ExploreTabs />

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import CommunityTabs from './ExploreTabs.vue'
</script>

<style scoped>
.header {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
</style>
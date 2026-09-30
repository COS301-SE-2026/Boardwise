<template>
    <PageContainer>
        <Navbar />
        <LiveTableRoom :event-id="route.params.id" />
    </PageContainer>
</template>

<script setup>
definePageMeta({
    middleware: 'auth'
})

import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import LiveTableRoom from '~/components/features/live-events/LiveTableRoom.vue'

const route = useRoute()
</script>
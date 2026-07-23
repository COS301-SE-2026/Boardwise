<template>
    <PageContainer>
        <Navbar/>
        <HelpHero />
        <HelpFAQ />
        <HelpContact />
    </PageContainer>
</template>

<script setup>
import PageContainer from '~/components/layout/PageContainer.vue'
import HelpHero from './HelpHero.vue'
import HelpFAQ from './HelpFAQ.vue'
import HelpContact from './HelpContact.vue'
import Navbar from '~/components/layout/Navbar.vue';
</script>
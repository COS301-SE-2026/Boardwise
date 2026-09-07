<template>
    <PageContainer>
        <Navbar/>
        <HelpHero />
        <HelpTutorials/>
        
        <HelpPopularGuides
                :search="search"
                :topic="activeTopic"
            />
        
        <HelpAiAssist/>
        <HelpFAQ />
        <HelpContact />
    </PageContainer>
</template>

<script setup>
import PageContainer from '~/components/layout/PageContainer.vue'
import HelpHero from './HelpHero.vue'
import HelpFAQ from './HelpFAQ.vue'
import HelpContact from './HelpContact.vue'
import Navbar from '~/components/layout/Navbar.vue'
import HelpTutorials from './HelpTutorials.vue'
import HelpPopularGuides from './HelpPopularGuides.vue'
import HelpAiAssist from './HelpAiAssist.vue'
</script>
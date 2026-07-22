<template>
    <LandingNavbar/>
    <main>
    <LandingHero/>
    <LandingGameStrip/>
    
    <LandingFeatures/>
    <LandingFeaturedGames/>
    <LandingCallToAction/>
    </main>
    <LandingFooter/>
</template>

<script setup>
import LandingCallToAction from './LandingCallToAction.vue'
import LandingFeaturedGames from './LandingFeaturedGames.vue'
import LandingFeatures from './LandingFeatures.vue'
import LandingFooter from './LandingFooter.vue'
import LandingGameStrip from './LandingGameStrip.vue'
import LandingHero from './LandingHero.vue'
import LandingNavbar from './LandingNavbar.vue'
</script>
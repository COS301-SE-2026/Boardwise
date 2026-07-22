<template>
    <v-container class="container py-16">

        <v-row class="mb-10"
        >
            <v-col
                cols="12" class="text-center"
            >
                <h2>
                    Explore Boardwise
                </h2>
                <p class="mt-4">
                    Everything you need to discover, collect and enjoy board games.
                </p>
            </v-col>
            </v-row>
            <v-row>
            <v-col
                v-for="feature in features"
                :key="feature.id"
                cols="12" 
                sm="6" 
                md="4"
            >
            <LandingFeatureCard :feature="feature"/>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import { getPlatformFeatures } from '~/services/landingService'
import LandingFeatureCard from './LandingFeatureCard.vue';

const features = getPlatformFeatures()
</script>
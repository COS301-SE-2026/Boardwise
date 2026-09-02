<template>
    <v-container class=" py-16">

        <v-row class="mb-14"
        >
            <v-col
                cols="12" 
                md="12"
                class="text-center"
            >
                <h2>
                    Everything Boardwise Offers
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
                md="6"
                lg="3"
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
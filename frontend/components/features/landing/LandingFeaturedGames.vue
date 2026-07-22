<template>
    <v-container class="container py-16">
        <v-row class="mb-10" align="center">
            <v-col cols="12" lg="6">
                <h2>
                    How<br>Boardwise<br>works
                </h2>

            </v-col>

            <v-col cols="12" md="6" class="d-flex align-center">
                <p>
                    Get started in minutes. Create your account, build your collection,
                    connect with players and discover everything Boardwise has to offer.
                </p>
            </v-col>
        </v-row>

            <v-row>
            <v-col v-for="step in steps"
                    :key="step.id"
                    cols="12"
                    sm="6"
                    md="4"
                    lg="2"
                    >
                <LandingGameCard :game="step"
                />
                </v-col>
                </v-row>
    </v-container>
</template>

<script setup>

import { onboardingSteps } from '~/services/mockData/landing.js'
import LandingGameCard from './LandingGameCard.vue';

const steps = onboardingSteps
</script>
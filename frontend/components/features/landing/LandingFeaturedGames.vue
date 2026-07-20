<template>
    <v-container class="py-16">
        <v-row class="mb-10">
            <v-col cols="12" lg="6">
                <v-chip color="primary" class="mb-4">
                    Supported games
                </v-chip>

                <h2 class="landing-heading">
                    Hundrends of games.
                </h2>

                <h2 class="landing-heading text-primary">
                    One intelligent guide.
                </h2>
            </v-col>

            <v-col cols="12" lg="6" class="d-flex align-center">
                <p class="landing-subtitle">
                    Whether you're learning Catan for the first time or setting up
                    an expansion, Boardwise helps you get playing faster.
                </p>
            </v-col>
        </v-row>

        <v-row>
            <v-col 
                v-for="game in games"
                :key="game.id"
                cols="12" sm="6" lg="4"
            >
                <LandingGameCard :game="game"/>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>

import { getFeaturedGames } from '~/services/landingService'
import LandingGameCard from './LandingGameCard.vue';

const games = getFeaturedGames()
</script>
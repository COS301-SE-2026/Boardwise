<template>
    <v-container class="py-16">

        <v-row 
            v-for="(feature, index) in features"
            :key="feature.title"
            align="center" class="mb-16"
        
        >
            <v-col
                cols="12" md="6" :order="index % 2 === 0 ? 1 : 2"
            >
                <v-img
                    :src="feature.image"
                    max-width="420" class="mx-auto"
                />
            </v-col>

            <v-col
                cols="12" md="6" :order="index % 2 === 0 ? 2 : 1"
            >

            <v-chip color="primary" class="mb-4">
                {{ feature.tag }}
            </v-chip>

            <h2 class="landing-heading mb-4">
                {{ feature.title }}
            </h2>

            <p class="landing-subtitle mb-6">
                {{  feature.description }}
            </p>

            <v-list bg-color="transparent">
                <v-list-item
                    v-for="item in feature.highlights"
                    :key="item"
                    prepend-icon="mdi-check-circle"
                >
                    <v-list-item-title>
                        {{ item }}
                    </v-list-item-title>
                </v-list-item>
            </v-list>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import { getPlatformFeatures } from '~/services/landingService'

const features = getPlatformFeatures()
</script>
<template>
    <v-container class="container py-16"
    >
        <v-row align="center" justify="space-between">

            <v-col cols="12" md="6">
                    <h1>
                        Connect.<br>Share.<br>Play.<br> All things board games.
                    </h1>
                    <div>
                        <span class="hero-badge hero-badge--connect">Connect</span>
                        <span class="hero-badge hero-badge--share">Share</span>
                        <span class="hero-badge hero-badge--play">Play</span>
                    </div>
                    <p class="mt-6 mb-8">
                        Access a library of your favourite board games, discover new games,
                        connect with other players, trade or sell games, all in one place.</p>

                    <div class="d-flex flex-wrap ga-4">
                        <BaseButton
                            variant="secondary"
                            @click="router.push('/library')"
                        >
                            Rulebooks
                        </BaseButton>

                        
                        <BaseButton @click="router.push('/auth/signup')">
                            Get started 
                        </BaseButton>

                    </div>
            </v-col>
           
        </v-row>
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
const router = useRouter()

</script>

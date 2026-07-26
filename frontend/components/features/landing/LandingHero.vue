<template>
    <v-container class="py-16"
    >
        <v-row align="center" justify="center">

            <v-col cols="12" md="7" class="text-center mx-auto">
                    <h1>
                        Play more. Learn faster.<br> 
                        For people who love the table.
                    </h1>
                    <div class="hero-badges">
                        <span class="hero-badge hero-badge--connect">Connect</span>
                        <span class="hero-badge hero-badge--share">Share</span>
                        <span class="hero-badge hero-badge--play">Play</span> 
                    </div>
                    <p class="mt-6 mb-8">
                        Access a library of your favourite board games,
                        discover new games, connect with other players,
                        trade or sell games, all in one place.</p>

                    <div class="d-flex justify-center flex-wrap ga-4">
                        <BaseButton
                            variant="outlined"
                            color="secondary"
                            @click="router.push('/library')"
                        >
                            Rulebooks
                        </BaseButton>

                        
                        <BaseButton 
                            color="white"
                            @click="router.push('/auth/signup')">
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

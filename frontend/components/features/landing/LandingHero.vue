<template>
    <v-container class="py-16"
    >
        <v-row align="center" >

            <v-col cols="12" md="12" class="text-start">
                    <div class="hero-heading">
                        <img
                            src="/images/BoarleySide.svg"
                            alt="Boarley"
                            class="hero-heading-mascot"
                        />
                    <h1>
                        Play more. Learn faster.<br> 
                        For people who love the table.
                    </h1>
                    </div>
                    <div class="hero-badges">
                        <span class="hero-badge hero-badge--connect">Connect</span>
                        <span class="hero-badge hero-badge--share">Share</span>
                        <span class="hero-badge hero-badge--play">Play</span> 
                    </div>
                    <p class="mt-6 mb-8">
                        Access a library of your favourite board games,
                        discover new games, connect with other players,
                        trade or sell games, all in one place.</p>

                    <div class="d-flex flex-wrap">
                        <BaseButton
                            variant="secondary"
                            @click="router.push('/library')"
                        >
                            Rulebooks
                        </BaseButton>

                        
                        <BaseButton 
                            class="ml-3"
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

<template>
    <v-container class="container py-16"
    >
        <v-row align="center" justify="space-between">

            <v-col cols="12" md="6">
                    <h1>
                            Play.<br>Connect.<br>Share.<br> All things board games.
                    </h1>
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
            <v-col 
                cols="12"
                md="6"
                class="d-flex flex-column align-center"
                >
                <v-img 
                    src="/images/landing/hero.svg" 
                    max-width="520" 
                    class="mx-auto"
                    />
              
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
const router = useRouter()

</script>

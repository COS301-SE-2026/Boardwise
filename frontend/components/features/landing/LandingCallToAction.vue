<template>
    <v-container class=" py-16 ">

        <v-sheet 
            rounded="xl"
            color="primary"
            class="pa-10 pa-md-16 overflow-hidden position-relative hero-cta"
        >
            <v-row align="center">
                <v-col cols="12" md="12" class="text-center">
                    <BaseButton variant="outlined" color="white" class="mb-4">
                        Ready to play?
                    </BaseButton>
                    
                    
            <p  class="cta-title text-h3 font-weight-bold mb-4">
                Spend less time reading.
                <br>More time playing.
            </p>

            <p class="text-body-1 text-white mb-8">
                Discover new board games, access rulebooks,
                connect with players and share games all in one place. </p>

            <div class="d-flex justify-center ga-4 flex-wrap">
                <BaseButton 
                    variant="outlined" 
                    color="white"
                    @click="router.push('/library')"
                    >

                    Rulebooks
                </BaseButton>

                <BaseButton
                    variant="outlined"
                    color="white"
                    @click="router.push('/auth/signup')"
                >
                    Get Started
                </BaseButton>
            </div>
        </v-col>
        
            </v-row>
            </v-sheet>

            
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
</script>
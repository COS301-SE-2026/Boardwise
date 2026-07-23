<template>
    <v-container class=" py-18">

        <v-sheet 
            rounded="xl"
            color="primary"
            class="pa-8 pa-md-16 overflow-hidden position-relative"
        >
            <v-row align="center" justify="space-between">
                <v-col cols="12" md="6">
                    <BaseButton color="white" class="mb-4">
                        Ready to play?
                    </BaseButton>
            <h2 class="text-h3 font-weight-bold text-white mb-4">
                Spend less time reading.<br>More time playing.
            </h2>

            <p class="text-body-1 text-medium-emphasis text-white mb-8">
                Access rulebooks of your favourite board games, discover new games, 
                connect with others players, rent or sell games - all in one place. 
            </p>

            <div class="d-flex ga-4 flex-wrap">
                <BaseButton 
                    variant="outlined" 
                    color="white"
                    @click="router.push('/library')"
                    >

                    Rulebooks
                </BaseButton>
            </div>
        </v-col>

                <v-col
                    cols="12"
                    md="5"
                    class="text-center"
                >
                    <v-icon
                        icon="mdi-"
                        size="100"
                        color="white"
                    />
                </v-col>
            </v-row>
            </v-sheet>

            
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
</script>
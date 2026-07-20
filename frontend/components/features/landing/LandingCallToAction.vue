<template>
    <v-container class="py-16">

        <v-sheet 
            rounded="xl"
            color="primary"
            class="pa-8 pa-md-16"
        >
            <v-row align="center" justify="space-between">
                <v-col cols="12" md="6">
                    <v-chip color="white" class="mb-4">
                        Ready to play?
                    </v-chip>
            <h2 class="text-h3 font-weight-bold text-white mb-4">
                Spend less time reading.<br>More time playing.
            </h2>

            <p class="text-body-1 text-medium-emphasis text-white mb-8">
                Discover new games, connect with players, trade board games,
                and never miss your next game night.
            </p>

            <div class="d-flex ga-4 flex-wrap">
                <BaseButton @click="router.push('/auth/signup')">
                    Get started
                </BaseButton>

                <BaseButton 
                    variant="outlined" 
                    color="white"
                    @click="router.push('/library')"
                    >

                    Browse library
                </BaseButton>
            </div>
        </v-col>

                <v-col
                    cols="12"
                    md="5"
                    class="text-center"
                >
                    <v-img
                        src="/images/landing/cta.svg"
                        max-width="420"
                        class="mx-auto"
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
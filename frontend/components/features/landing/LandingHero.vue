<template>
    <v-container
        fluid
        class="fill-height py-16"
    >
        <v-row align="center" justify="space-between">

            <v-col 
                cols="12"
                lg="6"
                order="2"
                order-lg="1"
                >
                <v-chip color="primary" class="mb-6">
                    Boarice AI powered setup wizard
                </v-chip>
                    <h1 class="landing-title">
                        OPEN THE BOX
                    </h1>

                    <h1 class="landing-title text-primary mb-6">
                        WE'LL HANDLE
                        <br>
                        THE RULEBOOK
                    </h1>
                    <p class="landing-subtitle mt-8">
                        From opening the box to your first turn,
                        Boardwise uses AI to simplify rulebooks,
                        guide setup and answer your questions in real time.    
                    </p>

                    <div class="d-flex flex-wrap ga-4">
                        <BaseButton @click="router.push('/auth/signup')">
                            Get started 
                        </BaseButton>

                        <BaseButton
                            variant="secondary"
                            @click="router.push('/library')"
                        >
                            Browse library
                        </BaseButton>
                    </div>
            </v-col>
            <v-col 
                cols="12"
                lg="5"
                order="1"
                order-lg="2"
                class="text-center"
                >
                <v-img src="/images/landing/hero.svg" max-width="550"/>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
const router = useRouter()

</script>

<style scoped>
.fill-height{
    min-height: 85vh;
}

.landing-title{
    font-size: 5rem;
    font-weight: 700;
}

.landing-subtitle{
    font-size: 1.3rem;
    max-width: 600px;
    margin: auto;
}
</style>
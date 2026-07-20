<template>
    <v-container
        fluid
        class=""
    >
        <v-row justify="center">

            <v-col 
                cols="12"
                md="7"
                >
                <div class="text-center">

                    <h1 class="landing-title">
                        Boardwise
                    </h1>

                    <p class="landing-subtitle mt-6">
                        Discover, play and share board games with friends.
                    </p>

                    <div class="d-flex justify-center ga-4 mt-10">
                        <BaseButton @click="router.push('/auth/signup')">
                            Get started
                        </BaseButton>

                        <BaseButton
                            variant="secondary"
                            @click="router.push('/library')"
                        >
                            Explore library
                        </BaseButton>
                    </div>
                </div>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';
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
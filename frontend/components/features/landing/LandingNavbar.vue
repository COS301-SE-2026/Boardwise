<template>
    <v-app-bar flat class="landing-navbar">
        <v-container class="landing-navbar__inner">
            <NuxtLink
                to="/"
                class="landing-navbar__brand"
                aria-label="Boardwise home"
            >
                Boardwise
            </NuxtLink>
           
            <div class="landing-navbar__actions">
                <BaseButton
                    variant="secondary"
                    @click="router.push('/auth/signin')"
                >
                    Sign In
                </BaseButton>

                <BaseButton
                    class="ml-3"
                    @click="router.push('/auth/signup')"
                >
                    Sign Up
                </BaseButton>
            </div>
        </v-container>
    </v-app-bar>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
</script>

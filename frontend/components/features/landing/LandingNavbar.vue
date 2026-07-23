<template>
    <v-app-bar
        flat
        class="px-8"
    >

    <h1>
        Boardwise
    </h1>

    <v-spacer/>

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
    </v-app-bar>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
</script>

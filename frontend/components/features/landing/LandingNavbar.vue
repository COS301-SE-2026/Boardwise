<template>
    <v-app-bar
        flat
        color="transparent"
        class="px-8"
    >

    <v-toolbar-title class="font-weight-bold">
        Boardwise
    </v-toolbar-title>

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
        Get started
    </BaseButton>
    </v-app-bar>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
</script>

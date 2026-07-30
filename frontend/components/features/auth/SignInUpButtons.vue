<template>
    <div>
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
</template>
<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import { useRouter } from 'vue-router'

const router = useRouter();
</script>
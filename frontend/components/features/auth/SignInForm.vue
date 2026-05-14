<template> 
    <AuthForm 
        title="SignIn"
        buttonText="SignIn"
        @submit="handleSignIn"
    />
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()

const handleSignIn = (data) => { 
    console.log('SignIn:', data)

    <!-- TEMP SIGNIN -->
    if (data.email && data.password) {
        router.push('/library')
    }
}
</script>
<template> 
    <AuthForm 
        title="SignUp"
        buttonText="Sign Up"
        @submit="handleSignUp"
    />
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()

const handleSignIn = (data) => { 
    console.log('SignUp:', data)

    // TEMP SIGNUP
    if (data.email && data.password) {
        router.push('/profile')
    }
}
</script>
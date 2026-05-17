<template> 
    <AuthForm 
        title="Sign In"
        buttonText="Sign In"
        :fields="fields"
        @submit="handleSignIn"
    />
    <p v-if="error" class="error">{{ error }}</p>
</template>

<script setup>
import AuthForm from './AuthForm.vue'
// Destructure what we need from the composable
const { login, error } = useAuth()

const fields = [
    { key: 'username', placeholder: 'Username', type: 'text' },
    { key: 'password', placeholder: 'Password', type: 'password' }
]

const handleSignIn = async (data) => {
    console.log('SignIn:', data)

    if (!data.username || !data.password) {
        error.value = 'Please fill in all fields.'
        return
    }

    // Call the composable function
    const success = await login({
        username: data.username,
        password: data.password
    })

    // If the composable successfully got a token, redirect
    if (success) {
        router.push('/library')
    }
}
</script>

<style scoped>
.error {
    color: #c0392b;
    text-align: center;
    margin-top: 8px;
    font-size: 14px;
}
</style>
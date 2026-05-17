<template>
<div class="signup-wrapper">
        <AuthForm
            title="Sign Up"
            buttonText="Sign Up"
            :fields="fields"
            @submit="handleSignUp"
        />
        <p v-if="localError" class="error">{{ localError }}</p>
        
        <p v-if="error" class="error">{{ error }}</p>
    </div>
</template>

<script setup>
import AuthForm from './AuthForm.vue'
const router = useRouter()
const { register, error } = useAuth()
const localError = ref('')

const fields = [
    { key: 'firstName', placeholder: 'First Name' },
    { key: 'lastName', placeholder: 'Last Name' },
    { key: 'username', placeholder: 'Username' },
    { key: 'emailAddress', placeholder: 'Email', type: 'email'    },
    { key: 'password', placeholder: 'Password', type: 'password' },
    { key: 'confirmPassword', placeholder: 'Confirm Password', type: 'password' }
]
const handleSignUp = async (data) => {
    console.log('SignUp:', data)
    localError.value = ''

    if (data.password !== data.confirmPassword) {
        error.value = 'Passwords do not match.'
        return
    }

    // Pass the clean data to the composable
    const success = await register({
        username: data.username,
        emailAddress: data.emailAddress,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName
    })

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
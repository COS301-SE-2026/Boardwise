<template> 
    <AuthForm 
        title="Sign In"
        buttonText="Sign In"
        :fields="fields"
        @submit="handleSignIn"
    />
    <p v-if="error" class="error">{{ error }}</p>

    <p class="redirect-text">
        Don’t have an account?
        <NuxtLink to="/auth/signup" class="redirect-link">
            Sign Up
        </NuxtLink>
    </p>
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()
const error = ref('')

// const { login, error } = useAuth()

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

    // const success = await login({
    //     username: data.username,
    //     password: data.password
    // })

    // if (success) 
    {
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

.redirect-text {
    text-align: center;
    margin-top: 16px;
    font-size: 14px;
    color: #666;
}

.redirect-link {
    color: #6C3BFF;
    font-weight: 600;
    text-decoration: none;
    margin-left: 4px;
}

.redirect-link:hover {
    text-decoration: underline;
}
</style>
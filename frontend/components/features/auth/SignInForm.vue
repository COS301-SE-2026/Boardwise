<template> 
    <div>
        <AuthForm 
            title="Sign In"
            buttonText="Sign In"
            :fields="fields"
            @submit="handleSignIn"
        />

        <v-alert 
            v-if="error"
            type="error"
            variant="tonal"
            class="mt-4"
            density="compact"
        >
            {{ error }}
        </v-alert>

        <p class="text-center text-body-2 mt-4 text-medium-emphasis redirect-text">
            Don’t have an account?
            <NuxtLink to="/auth/signup" class="text-primary font-weight-bold ml-1 redirect-link">
                Sign Up
            </NuxtLink>
        </p>
    </div>
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()
const { login, error, loading} = useAuth()

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

    const success = await login({
        username: data.username,
        password: data.password
    })

    if (success){
        router.push('/library')
    }
}
</script>
<template> 
    <v-container 
        fluid
        class="auth-page d-flex align-center justify-center"
    >
        <div class="auth-wrapper">
            <AuthForm 
                title="Sign In"
                buttonText="Sign In"
                :fields="fields"
                @submit="handleSignIn"
            >
            
                <template #after-fields>
                    <p class="text-end text-body-2 forgot-link">
                        <NuxtLink to="/auth/forgotpassword" class="text-primary">
                            Forgot Password?
                        </NuxtLink>
                    </p>
                </template>

            </AuthForm>

            <v-alert 
                v-if="error"
                type="error"
                variant="tonal"
                class="mt-4"
                density="compact"
            >
                {{ error }}
            </v-alert>

            <p class="text-center text-body-2 mt-4 text-medium-emphasis">
                Don’t have an account?
                <NuxtLink to="/auth/signup" class="text-primary font-weight-bold ml-1">
                    Sign Up
                </NuxtLink>
            </p>
        </div>
    </v-container>
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()
const { login, error, loading} = useAuth()

const fields = [
    { key: 'username', label: 'Username', type: 'text', rules: [required()]},
    { key: 'password', label: 'Password', type: 'password', rules: [required()] }
]

const handleSignIn = async (data) => {

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
<template>
    <div>
        <AuthForm
            title="Sign Up"
            buttonText="Sign Up"
            :fields="fields"
            @submit="handleSignUp"
        />
        
            <v-alert 
                v-if="localError || error"
                type="error"
                variant="tonal"
                class="mt-4"
                density="compact"
            >
                {{ localError || error }}
            </v-alert>

        <p class="text-center text-body-2 mt-4 text-medium-emphasis">
            Already have an account?
            <NuxtLink to="/auth/signin" class="text-primary font-weight-bold ml-1">
                Sign In
            </NuxtLink>
        </p>
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
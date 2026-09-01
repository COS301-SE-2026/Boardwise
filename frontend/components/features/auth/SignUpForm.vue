<template>
    <v-container
        fluid 
        class="auth-page d-flex align-center justify-center"
    >
        <div class="auth-wrapper">
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
                <NuxtLink to="/auth/onboarding" class="text-primary font-weight-bold ml-1">
                    Sign In
                </NuxtLink>
            </p>
        </div>
    </v-container>
</template>

<script setup>
import AuthForm from './AuthForm.vue'
const router = useRouter()
const { register, error } = useAuth()
const localError = ref('')

const fields = [
    { key: 'firstName', label: 'First Name' , rules: [required()]},
    { key: 'lastName', label: 'Last Name' , rules: [required()]},
    { key: 'username', label: 'Username' , rules: [required(), minLength(3)]},
    { key: 'emailAddress', label: 'Email', type: 'email' , rules: [required(), isEmail()]},
    { key: 'password', label: 'Password', type: 'password' , rules: [required(), minLength(8)]},
    { key: 'confirmPassword', label: 'Confirm Password', type: 'password' , rules: [required()]}
]
const handleSignUp = async (data) => {
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
.auth-page {
    min-height: calc(100vh - 80px);
    padding: 4rem 1.5rem;
}

.auth-wrapper {
    width: 100%;
    max-width: 520px;
}

.auth-link{
    text-align: center;
    margin-top: 1.5rem;
    color: var(--color-text-muted);
}

.auth-link a{
    color: var(--color-primary);
    font-weight: var(--fw-bold);
    text-decoration: none;
}

.auth-link a:hover{
    text-decoration: underline;
}
</style>
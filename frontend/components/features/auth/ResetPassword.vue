<template>
    <v-container fluid class="auth-page d-flex align-center justify-center">
        <div class="auth-wrapper">
            <AuthForm
                v-if="token"
                title="Set a New Password"
                buttonText="Reset Password"
                :fields="fields"
                @submit="handleResetPassword"
            />

            <v-alert v-else type="error" variant="tonal" density="compact">
                This reset link is invalid or has expired. Please request a new one. 
            </v-alert>

            <v-alert 
                v-if="localError || error"
                type="error"
                variant="tonal"
                class="mt-4"
                density="compact"
            >
                {{  localError || error }}
            </v-alert>

            <p class="text-center text-body-2 mt-4 text-medium-emphasis">
                <NuxtLink to="/auth/forgotpassword" class="text-primary font-weight-bold ml-1">
                    Request a new link
                </NuxtLink>
            </p>
        </div>
    </v-container>
</template>

<script setup>
import AuthForm from './AuthForm.vue';

const route = useRoute()
const router = useRouter()
const { resetPassword, error } = useAuth()
const localError = ref('')

const token = route.query.token

const fields = [
    { key: 'password', placeholder: 'New Password', type:'password'},
    { key: 'confirmPassword', placeholder: 'Confirm New Password', type:'password'}
]

const handleResetPassword = async (data) => {
    localError.value = ''
    if(data.password !== data.confirmPassword) {
        localError.value = 'Passwords do not match.'
        return
    }

    const success = await resetPassword({ token, password: data.password })

    if(success) {
        router.push('/auth/signin')
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
</style>
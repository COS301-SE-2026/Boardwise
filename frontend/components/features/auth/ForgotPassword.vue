<template>
    <v-container fluid class="auth-page d-flex align-center justify-center">
        <div class="auth-wrapper">
            <AuthForm
                data-test="auth-form"
                v-if="!emailSent"
                title="Forgot Your Password?"
                subtitle="There is nothing to worry aboutm we'll send you a message to reset your password!"
                buttonText="Send Reset Link"
                :fields="fields"
                @submit="handleForgotPassword"
            />

            <BaseCard v-else class="auth-card text-center" data-test="reset-sent-card">
                <v-icon size="48" color="success" class="mb-3">
                    mdi-email-check-outline
                </v-icon>
                <h2 class="form-title">Check your email</h2>
                <p class="text-body-2 text-medium-emphasis">
                    We've sent a reset link <strong>{{  submittedEmail }}</strong>.
                    Follow the link to set a new password, then you'll be sent to sign in.
                </p>
            </BaseCard>

            <v-alert 
                v-if="error"
                type="error"
                variant="tonal"
                class="mt-4"
                density="compact"
            >
                {{  error }}
            </v-alert>

            <p class="text-center text-body-2 mt-4 text-medium-emphasis">
                Remember Password?
                <NuxtLink to="/auth/signin" class="text-primary font-weight-bold ml-1">
                    Sign In
                </NuxtLink>
            </p>
        </div>
    </v-container>
</template>

<script setup>
import AuthForm from './AuthForm.vue';
import BaseCard from '~/components/ui/BaseCard.vue';

const { forgotPassword, error } = useAuth()

const emailSent = ref(false)
const submittedEmail = ref('')

const fields = [
    { key: 'emailAddress', label: 'Email Address', type:'email', rules: [required(), isEmail()]}
]

const handleForgotPassword = async (data) => {
    if(!data.emailAddress) {
        error.value = 'Please enter your email address.'
        return
    }

    const success = await forgotPassword(data.emailAddress)

    if(success) {
        submittedEmail.value = data.emailAddress
        emailSent.value = true
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
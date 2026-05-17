<template> 
    <AuthForm 
        title="Sign Up"
        buttonText="Sign Up"
        :fields="fields"
        @submit="handleSignUp"
    />
    <p v-if="error" class="error">{{ error }}</p>
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()
const error = ref('')

const fields = [
    { key: 'name',            placeholder: 'First Name'                         },
    { key: 'surname',         placeholder: 'Last Name'                          },
    { key: 'username',        placeholder: 'Username'                           },
    { key: 'email',           placeholder: 'Email',            type: 'email'    },
    { key: 'password',        placeholder: 'Password',         type: 'password' },
    { key: 'confirmPassword', placeholder: 'Confirm Password', type: 'password' }
]

const handleSignUp = (data) => {
    console.log('SignUp:', data)

    if (data.password !== data.confirmPassword) {
        error.value = 'Passwords do not match.'
        return
    }

    error.value = ''

    if (data.email && data.password) {
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
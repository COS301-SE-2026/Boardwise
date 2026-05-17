<template> 
    <AuthForm 
        title="Sign In"
        buttonText="Sign In"
        :fields="fields"
        @submit="handleSignIn"
    />
</template>

<script setup>
import AuthForm from './AuthForm.vue'

const router = useRouter()

const fields = [
    { key: 'email',    placeholder: 'Email',    type: 'email'    },
    { key: 'password', placeholder: 'Password', type: 'password' }
]

const handleSignIn = (data) => { 
    console.log('SignIn:', data)

    if (data.email && data.password) {
        router.push('/library')
    }
}
</script>
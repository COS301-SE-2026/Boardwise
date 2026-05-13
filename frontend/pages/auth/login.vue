<template> 
    <div class="container">
        <AuthForm 
            title="Login"
            buttonText="Login"
            @submit="handleLogin"
        />
    </div>
</template>

<script setup>
    import AuthForm from '~/components/features/auth/AuthForm.vue'

    const router = useRouter()

    const handleLogin = (data) => {
        console.log('LOGIN DATA', data)

        if (data.email && data.password) {
            router.push('/profile')
        }
    }
</script>

<style scoped>
.container {
    max-width: 400px;
    margin: 100px auto;
}
</style>
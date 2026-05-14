<template> 
    <div class="container">
        <AuthForm 
            title="SignIn"
            buttonText="SignIn"
            @submit="handleSignIn"
        />
    </div>
</template>

<script setup>
    import AuthForm from '~/components/features/auth/AuthForm.vue'

    const router = useRouter()

    const handleSignIn = (data) => {
        console.log('SIGNIN DATA', data)

        if (data.email && data.password) {
            router.push('/library')
        }
    }
</script>

<style scoped>
.container {
    max-width: 400px;
    margin: 100px auto;
}
</style>
<template>
  <div class="container">
    <AuthForm
      title="Register"
      buttonText="Register"
      @submit="handleRegister"
    />
  </div>
</template>

<script setup>
    import AuthForm from '~/components/features/auth/AuthForm.vue'

    const router = useRouter()

    const handleRegister = (data) => {
    console.log('REGISTER DATA:', data)

    router.push('/profile')
    }
</script>

<style scoped>
.container {
  max-width: 400px;
  margin: 100px auto;
}
</style>
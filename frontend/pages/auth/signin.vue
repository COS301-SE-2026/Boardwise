<template> 
    <div class="container">
        <SignInForm />
    </div>
</template>

<script setup>
import SignInForm from '~/components/features/auth/SignInForm.vue'
</script>

<style scoped>
.container {
    max-width: 400px;
    margin: 100px auto;
}
</style>
<template>
  <div class="container">
    <SignUpForm />
  </div>
</template>

<script setup>
import SignUpForm from '~/components/features/auth/SignUpForm.vue'
</script>

<style scoped>
.container {
  max-width: 400px;
  margin: 100px auto;
}
</style>
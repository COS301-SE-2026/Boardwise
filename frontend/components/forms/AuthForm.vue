<template>
  <div class="card">
    <h2>{{ title }}</h2>

    <input v-model="email" placeholder="Email" />
    <input v-model="password" type="password" placeholder="Password" />

    <button @click="submitForm">
      {{ buttonText }}
    </button>
  </div>
</template>

<script setup>
    const props = defineProps({
    title: String,
    buttonText: String
    })

    const emit = defineEmits(['submit'])

    const email = ref('')
    const password = ref('')

    const submitForm = () => {
    emit('submit', {
        email: email.value,
        password: password.value
    })
    }
</script>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border-radius: 10px;
  background: #f5f5f5;
}
</style>
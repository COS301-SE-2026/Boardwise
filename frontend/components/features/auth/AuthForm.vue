<template>
  <div class="card">
    <h2>{{ title }}</h2>

    <BaseInput v-model="email" placeholder="Email" />
    <BaseInput v-model="password" type="password" placeholder="Password" />

    <BaseButton @click="submitForm">
      {{ buttonText }}
    </BaseButton>
  </div>
</template>

<script setup>
  import BaseInput from '~/components/ui/BaseInput.vue'
  import BaseButton from '~/components/ui/BaseButton.vue'

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
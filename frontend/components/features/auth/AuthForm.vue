// Reusable form shell 
// Generic 

<template>
    <BaseCard>
        <div class="form">
            <h2>{{ title }}</h2>

            <BaseInput 
            v-model="email"
            type="email"
            placeholder="Email"
            />

            <BaseInput 
            v-model="password"
            type="password"
            placeholder="Password"
            />

            <BaseButton @click="submitForm"?
                {{ buttonText }}
            </BaseButton>
        </div>
    </BaseCard>
<template>

<script setup>
import BaseButton from '~components/ui/BaseButton.vue'
import BaseInput from '~components/ui/BaseInput.vue'
import BaseCard from '~components/ui/BaseCard.vue'

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
.form {
    display: flex;
    flex-direction: column;
    gap: 16px;
}  

h2 {
    text-align: center;
}
</style>
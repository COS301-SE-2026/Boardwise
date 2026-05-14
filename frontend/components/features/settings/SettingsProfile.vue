<template>
    <BaseCard>
        <div class="profile">
            <h2>General</h2>
            <BaseInput v-model="form.firstName" placeholder="Change first name" />
            <BaseInput v-model="form.lastName"  placeholder="Change last name"  />
            <BaseInput v-model="form.username"  placeholder="Change username"  />
            <BaseInput v-model="form.email"     type="email"     placeholder="Change email" />
            <BaseInput v-model="form.bio"       type="textarea"  placeholder="Change bio"   />
            <button @click="emit('save', form)">Save changes</button>
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue';
import BaseCard from '@/components/ui/BaseCard.vue';
import BaseInput from '@/components/ui/BaseInput.vue';

const form = ref({
    firstName: ' ',
    lastName: ' ',
    username: '',
    email: ' ',
    bio: ' '
});

const themes = [
    { label: 'Light', value: 'light' },
    { label: 'Dark', value: 'dark' },
    { label: 'System', value: 'system' },
]
</script>

<style scoped>
    .profile {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

button {
    align-self: flex-start;
}


</style>
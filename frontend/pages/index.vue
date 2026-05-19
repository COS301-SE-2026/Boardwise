<template> 
    <div class="container">
        <h1>Boardwise</h1>
        <p>Your board game hub</p>
    
        <div class="actions">
            <NuxtLink to="/auth/signin">
                <BaseButton>Sign In</BaseButton>
            </NuxtLink>

            <NuxtLink to="/auth/signup">
                <BaseButton>Sign Up</BaseButton>
            </NuxtLink>

        </div>
        
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
</script>

<style scoped>
.container {
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 16px;
}

.actions {
    display: flex;
    gap: 12px;
}

</style>
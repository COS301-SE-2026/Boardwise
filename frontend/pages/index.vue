<template> 
    <div class="container">
        <h1>Boardwise</h1>
        <p>Your board game hub</p>
    
        <div class="actions">
            <NuxtLink to="/auth/login">
                <BaseButton>Login</BaseButton>
            </NuxtLink>

            <NuxtLink to="/auth/register">
                <BaseButton>Register</BaseButton>
            </NuxtLink>
        </div>
    </div>
</template>

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
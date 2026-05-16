<template> 
    <nav class="navbar">

        <NuxtLink to="/" class="logo">
            Boardwise
        </NuxtLink>

        <div class="links">
            <NuxtLink to="/library">
                Library
            </NuxtLink> 

            <NuxtLink to="/marketplace">
                Marketplace
            </NuxtLink>

            <NuxtLink to="/profile">
                Community
            </NuxtLink>

            <NuxtLink to="/profile">
                Events
            </NuxtLink>

            <NuxtLink to="/profile">
                Profile
            </NuxtLink>
        </div>
    </nav>
</template>

<style scoped>
.navbar {
    height: 70px;

    display: flex;
    justify-content: space-between;
    align-items: center;

    padding: 0 24px;

    border-bottom: 1px solid #eee;
    background: white;
}

.logo {
    font-size: 20px;
    font-weight: bold;

    text-decoration: none;

    color: #6C3BFF;
}

.links {
    display: flex;
    gap: 20px;
}

.links a {
    text-decoration: none;
    color: #333;
}
</style>
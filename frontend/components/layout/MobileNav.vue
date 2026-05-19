<template> 
    <nav class="movile-nav">

        <NuxtLink to="/">
            Home
        </NuxtLink>
            
        <NuxtLink to="/marketplace">
            marketplace
        </NuxtLink>

        <NuxtLink to="/library">
            Library
        </NuxtLink>

        <NuxtLink to="/community">
            Community
        </NuxtLink>

        <NuxtLink to="/profile">
            Events
        </NuxtLink>

        <NuxtLink to="/profile">
            Profile
        </NuxtLink>
    </nav>
</template>

<style scoped>
.mobile-nav {
    position: fixed;
    bottom: 0;
    left: 0;

    width: 100%;
    height: 70px;

    background: white;
    
    display: flex;
    justify-content: space-around;
    align-items: center;

    border-top: 1px solid #ddd;
}

.mobile-nav a {
    text-decoration: none;
    color: #444;

    font-size: 14px;
}
</style>
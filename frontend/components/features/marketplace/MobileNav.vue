<template>
    <nav class="mobile-nav">

        <NuxtLink to="/">
            Home
        </NuxtLink>

        <NuxtLink to="/">
            Market
        </NuxtLink>

        <NuxtLink to="/">
            Library
        </NuxtLink>

        <NuxtLink to="/">
            Profile
        </NuxtLink>
    </nav>
</template>

<style scoped> 
.mobile-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;

    display: flex;
    justify-content: space-around;

    padding: 16px;
    background: white;
    border-top: 1px solid #ddd;
}

a {
    text-decoration: none;
    color: #333;
}
</style>
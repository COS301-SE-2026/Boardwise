<template> 
    <nav class="movile-nav">

    <NuxtLink to="/" class="nav-item">
      <v-icon>mdi-home</v-icon>
      <span>Home</span>
    </NuxtLink>
            
    <!-- <NuxtLink to="/" class="nav-item">
      <v-icon>mdi-home</v-icon>
      <span>Home</span>
    </NuxtLink> -->

    <NuxtLink to="/library" class="nav-item">
      <v-icon>mdi-library</v-icon>
      <span>Home</span>
    </NuxtLink>

    <NuxtLink to="/marketplace" class="nav-item">
      <v-icon>mdi-store</v-icon>
      <span>Library</span>
    </NuxtLink>

    <!-- <NuxtLink to="/community" class="nav-item">
      <v-icon>mdi-account-event</v-icon>
      <span>Events</span>
    </NuxtLink> -->

    <NuxtLink to="/events" class="nav-item">
      <v-icon>mdi-account-group</v-icon>
      <span>Community</span>
    </NuxtLink>

    <NuxtLink to="/profile" class="nav-item">
      <v-icon>mdi-account</v-icon>
      <span>Profile</span>
    </NuxtLink>
    </nav>
</template>

<style scoped>
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 64px;
  background: white;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #ddd;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-decoration: none;
  color: #888;
  font-size: 11px;
}

.nav-item.router-link-active {
  color: #6D0037;
}

.nav-item.router-link-active .v-icon {
  color: #6D0037;
}
</style>
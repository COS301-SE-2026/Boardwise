<template> 
  <v-app-bar 
    flat 
    border="b" 
    color="surface" 
    height="72" 
  >

  <div class="navbar">
    <div class="left">
      <v-app-bar-nav-icon 
        v-if="!lgAndUp"
        @click="drawer = !drawer"
      />

      <NuxtLink to="/" class="logo ">
          Boardwise
      </NuxtLink>

    </div>

    <!-- Desktop Search -->
    <div v-if="lgAndUp" class="center">
      <v-text-field 
        placeholder="Search games, users, rules..."
        prepend-inner-icon="mdi-magnify"
        class="search"
      />

      <v-btn 
       color="primary" 
       rounded="pill" 
       height="40"
       @click="$emit('ask-ai')"
      >
        <v-icon start>mdi-robot</v-icon>
        Ask AI
      </v-btn>
    </div>

    <!-- Desktop Navigation -->
    <div v-if="lgAndUp" class="right">
        <NuxtLink to="/library" class="nav-link">Library</NuxtLink> 
        <NuxtLink to="/marketplace" class="nav-link">Marketplace</NuxtLink>
        <!-- <NuxtLink to="/community" class="nav-link">Community</NuxtLink> -->
        <NuxtLink to="/events" class="nav-link">Events</NuxtLink>
        <NuxtLink to="/profile" class="nav-link">Profile</NuxtLink>
        <!-- <NuxtLink to="/chats" class="nav-link">Chat</NuxtLink> -->
        <LogOutButton />
    </div>

    <!-- Mobile -->

    <div v-if="!lgAndUp" class="mobile">
      <v-menu 
        :close-on-content-click="false" 
        location="bottom end" 
      >

        <template #activator="{ props }">
          <v-btn 
            icon="mdi-magnify" 
            v-bind="props" 
            variant="text" 
          />
        </template>

        <!-- Search (Mobile) -->
        <v-card class="pa-2" min-width="280">
          <v-text-field
            placeholder="Search..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            rounded="pill"
            hide-details
            autofocus
          />
        </v-card>
      </v-menu>

      <v-btn 
        icon 
        color="primary" 
        @click="$emit('ask-ai')"
      >
        <v-icon>mdi-robot</v-icon>
      </v-btn>
    </div>
  </div>
</v-app-bar>

<v-navigation-drawer 
  v-model="drawer" 
  temporary
  location="left" 
  color="surface"
>
  <v-list nav density="compact">
    <v-list-item prepend-icon="mdi-bookshelf" title="Library" to="/library" @click="drawer = false" />
    <v-list-item prepend-icon="mdi-store" title="Marketplace" to="/marketplace" @click="drawer = false" />
    <!-- <v-list-item prepend-icon="mdi-account-group" title="Community" to="/community" @click="drawer = false" /> -->
    <v-list-item prepend-icon="mdi-calendar" title="Events" to="/events" @click="drawer = false" />
    <v-list-item prepend-icon="mdi-account" title="Profile" to="/profile" @click="drawer = false" />
    <!-- <v-list-item prepend-icon="mdi-message" title="Chat" to="/chats" @click="drawer = false" /> -->
  </v-list>

    <template #append>
      <div class="pa-4">
        <LogOutButton block />
      </div>
    </template>
</v-navigation-drawer>

</template>

<script setup>
import { ref } from 'vue'
import { useDisplay } from 'vuetify'

import LogOutButton from '~/components/features/auth/LogOutButton.vue'

defineEmits(['ask-ai'])

const drawer = ref(false)
const { lgAndUp } = useDisplay()
</script>

<style scoped>
:deep(.v-toolbar__content) {
  padding: 0 24px;
}

.navbar {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 32px;
}

.left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.center {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
}

.logo {
  font-family: var(--font-display);
  font-size: var(--fs-h1);
  font-weight: var(--fw-bold);
  text-decoration: none;
  color: var(--obsidian);
  white-space: nowrap;
}

.logo:hover {
  color: var(--color-primary-hover);
}

.search {
  width: 420px;
}

.nav-link {
  color: var(--color-text);
  text-decoration: none;
  font-weight: var(--fw-medium);
  transition: color .2s;
}

.nav-link:hover,
.nav-link.router-link-active,
.nav-link.router-link-exact-active {
  color: var(--obsidian);
  font-weight: var(--fw-bold);
}

@media (max-width:1279px) {
  :deep(.v-toolbar__content) {
    padding: 0 12px;
  }

  .navbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .logo {
    font-size: 2rem;
  }

  .mobile .v-btn {
    width: 36px;
    min-width: 36px;
    height: 36px;
  }

  .left {
    flex: 1;
    min-width: 0;
  }
}

:deep(.v-field--outlined) {
  --v-field-border-color: var(--color-border-strong);
  --v-field-border-opacity: 1;
}

:deep(.v-field--focused) {
  --v-field-border-color: var(--color-primary) !important;
}
</style>
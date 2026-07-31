<template> 
  <v-app-bar 
    flat border="b" 
    color="surface" 
    height="72" 
    class="px-2 px-md-4"
  >
      
    <v-app-bar-nav-icon 
      class="d-lg-none ml-1 text-primary"
      @click="drawer = !drawer"
    />

    <NuxtLink to="/" class="logo ml-2 ml-lg-4 mr-lg-6">
        Boardwise
    </NuxtLink>

    <div class="center d-none d-sm-flex">
      <v-text-field 
        placeholder="Search games, users, rules..."
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        rounded="pill"
        hide-details
        class="search-input"
      />

      <v-btn 
       color="primary" 
       rounded="pill" 
       class="text-none font-weight-bold"
       @click="$emit('ask-ai')"
      >
        <v-icon start size="18">mdi-robot</v-icon>
        <span class="d-none d-md-inline">Ask AI</span>
      </v-btn>
    </div>

    <v-spacer class="d-sm-none" />

    <v-menu :close-on-content-click="false" location="bottom end" class="d-sm-none">
      <template #activator="{ props }">
        <v-btn icon="mdi-magnify" v-bind="props" class="d-sm-none" color="on-surface" />
      </template>

      <v-card min-width="280" class="pa-2" color="surface">
        <v-text-field
          placeholder="Search..."
          prepend-innner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          rounded="pill"
          hide-details
          autofocus
        />
      </v-card>

    </v-menu>

    <v-btn icon color="primary" class="d-sm-none mr-1" aria-label="Ask AI" @click="$emit('ask-ai')">
      <v-icon>mdi-robot</v-icon>
    </v-btn>

    <div class="links mr-4 d-none d-lg-flex">
        <NuxtLink to="/library" class="nav-link">Library</NuxtLink> 
        <NuxtLink to="/marketplace" class="nav-link">Marketplace</NuxtLink>
        <NuxtLink to="/community" class="nav-link">Community</NuxtLink>
        <NuxtLink to="/events" class="nav-link">Events</NuxtLink>
        <NuxtLink to="/profile" class="nav-link">Profile</NuxtLink>
        <NuxtLink to="/chats" class="nav-link">Chat</NuxtLink>
        <LogOutButton />
    </div>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" temporary location="left" color="surface">
    <v-list nav density="compact" class="mt-4">
      <v-list-item prepend-icon="mdi-bookshelf" title="Library" to="/library" class="drawer-link" />
      <v-list-item prepend-icon="mdi-store" title="Marketplace" to="/marketplace" class="drawer-link" />
      <v-list-item prepend-icon="mdi-account-group" title="Community" to="/community" class="drawer-link" />
      <v-list-item prepend-icon="mdi-calendar" title="Events" to="/events" class="drawer-link" />
      <v-list-item prepend-icon="mdi-account" title="Profile" to="/profile" class="drawer-link" />
      <v-list-item prepend-icon="mdi-message" title="Chat" to="/chats" class="drawer-link" />
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
import LogOutButton from '~/components/features/auth/LogOutButton.vue'
defineEmits(['ask-ai'])

const drawer = ref(false)
</script>

<style scoped>
.logo {
  font-family: var(--font-display);
  font-size: var(--fs-h3);
  font-weight: var(--fw-bold);
  text-decoration: none;
  color: var(--color-primary);
  white-space: nowrap;
  transition: color var(--transition-fast);
}

.logo:hover {
  color: var(--color-primary-hover);
  text-decoration: none;
}

.center {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1 1 auto;
  justify-content: center;
  max-width: 500px;
  margin: 0 var(--space-4);
}

.search-input {
  width: 100%;
  max-width: 320px;
}

.links {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  white-space: nowrap;
}

.nav-link {
  font-family: var(--font-body);
  font-size: var(--fs-body);
  font-weight: var(--fw-medium);
  text-decoration: none;
  color: var(--color-text);
  transition: color var(--transition-fast);
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--color-primary);
  font-weight: var(--fw-bold);
  text-decoration: none;
}

.drawer-link {
  font-family: var(--font-body);
  color: var(--color-text);
}

:deep(.v-field--outlined) {
  --v-field-border-color: var(--color-border-strong);
  --v-field-border-opacity: 1;
}

:deep(.v-field--focused) {
  --v-field-border-color: var(--color-primary) !important;
}
</style>
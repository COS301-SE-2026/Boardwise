<template> 
  <v-app-bar 
    flat 
    border="b" 
    color="surface" 
    height="72" 
    class="app-bar"
  >

  <div class="navbar">
    <div class="left">
      <v-app-bar-nav-icon 
        v-if="!lgAndUp"
        @click="drawer = !drawer"
      />

      <NuxtLink data-test="nuxt-link" to="/library" class="logo ">
          Boardwise
      </NuxtLink>

    </div>

    <!-- Desktop Search -->
    <div v-if="lgAndUp" class="center">
      <GlobalSearch class="search" />
    </div>

    <!-- Desktop Navigation -->
    <div v-if="lgAndUp" class="right">
        <NuxtLink to="/library" class="nav-link">Library</NuxtLink> 
        <NuxtLink to="/marketplace" class="nav-link">Marketplace</NuxtLink>
        <NuxtLink to="/social" class="nav-link">Social</NuxtLink>
        <NuxtLink to="/events" class="nav-link">Events</NuxtLink>

        <NotificationBell />

        <BaseDropdown 
          aria-label="Account menu"
          :menu-props="{
            openOnHover: true,
            closeOnContentClick: false,
            location: 'bottom end',
            offset: 8
          }"
          :list-props="{
            nav: true,
            density: 'compact',
            minWidth: 200
          }"
          icon
          variant="text"
          to="/profile"
        >
          <template #activator>
              <v-icon size="28">mdi-account-circle</v-icon>
          </template>

          <v-list-item
              prepend-icon="mdi-chat-outline"
              title="Chats"
              to="/chats"
          />
          <v-list-item
              prepend-icon="mdi-cog-outline"
              title="Settings"
              to="/settings"
          />
          <v-divider class="my-1" />
          <v-list-item class="px-2">
              <LogOutButton block />
          </v-list-item>
        </BaseDropdown> 
    </div>

    <!-- Mobile -->

    <div v-if="!lgAndUp" class="mobile">
      <BaseButton
        icon
        variant="text"
        aria-label="Search"
        :aria-expanded="String(mobileSearchOpen)"
        @click="mobileSearchOpen = !mobileSearchOpen"
      >
        <v-icon size="26">{{ mobileSearchOpen ? 'mdi-close' : 'mdi-magnify' }}</v-icon>
      </BaseButton>

      <NotificationBell />

      <BaseDropdown
        aria-label="Account menu"
        :menu-props="{
          closeOnContentClick: false,
          location: 'bottom end'
        }"
        :list-props="{
          nav: true,
          density: 'compact',
          minWidth: 200
        }"
        icon
        variant="text"
        to="/profile"
      >
        <template #activator>
            <v-icon size="26">mdi-account-circle</v-icon>
        </template>

        <v-list-item 
            prepend-icon="mdi-chat-outline"
            title="Chats"
            to="/chats"
          />

          <v-list-item 
            prepend-icon="mdi-cog-outline"
            title="Settings"
            to="/settings"
          />

          <v-divider class="my-1" />
          <v-list-item class="px-2">
            <LogOutButton block />
          </v-list-item>
      </BaseDropdown>
    </div>
  </div>
</v-app-bar>

<div v-if="!lgAndUp && mobileSearchOpen" class="mobile-search-bar">
  <GlobalSearch placeholder="Search..." @select="mobileSearchOpen = false" />
</div>

<v-navigation-drawer 
  v-model="drawer" 
  temporary
  location="left" 
  color="surface"
>
  <v-list nav density="compact">
    <v-list-item prepend-icon="mdi-bookshelf" title="Library" to="/library" @click="drawer = false" />
    <v-list-item prepend-icon="mdi-store" title="Marketplace" to="/marketplace" @click="drawer = false" />
    <v-list-item prepend-icon="mdi-account-group" title="Social" to="/social" @click="drawer = false" />
    <v-list-item prepend-icon="mdi-calendar" title="Events" to="/events" @click="drawer = false" />
  </v-list>
</v-navigation-drawer>

</template>

<script setup>
import { ref } from 'vue'
import { useDisplay } from 'vuetify'

import LogOutButton from '~/components/features/auth/LogOutButton.vue'
import GlobalSearch from '../features/search/GlobalSearch.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseDropdown from '../ui/BaseDropdown.vue'
import NotificationBell from '../features/notifications/NotificationBell.vue'

const drawer = ref(false)
const mobileSearchOpen = ref(false)

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
  transition: color 0.2s;
}

.nav-link:hover,
.nav-link.router-link-active,
.nav-link.router-link-exact-active {
  color: var(--color-primary);
  font-weight: var(--fw-bold);
  text-decoration: none;
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

  .left {
    flex: 1;
    min-width: 0;
  }
}
.mobile {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.v-field--outlined) {
  --v-field-border-color: var(--color-border-strong);
  --v-field-border-opacity: 1;
}

:deep(.v-field--focused) {
  --v-field-border-color: var(--color-primary) !important;
}

.app-bar {
  overflow: visible !important;
}

.mobile-search {
  overflow: visible !important;
}
</style>
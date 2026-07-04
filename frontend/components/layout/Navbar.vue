<template> 
  <v-app-bar flat border="b" color="surface" height="70">

    <NuxtLink to="/" class="logo ml-4">
        Boardwise
    </NuxtLink>

    <div class="center">
        <v-text-field 
          placeholder="Search games, users, rules..."
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          rounded="pill"
          hide-details
          style="min-width: 260px;"
        />

        <v-btn color="primary" rounded="pill" @click="$emit('ask-ai')">
        <v-icon start>mdi-robot</v-icon>
        Ask AI
      </v-btn>
    </div>

    <div class="links mr-4">
        <NuxtLink to="/library">
            Library
        </NuxtLink> 

        <NuxtLink to="/marketplace">
            Marketplace
        </NuxtLink>

        <NuxtLink to="/community">
            Community
        </NuxtLink>

        <NuxtLink to="/events">
            Events
        </NuxtLink>

        <NuxtLink to="/profile">
            Profile
        </NuxtLink>
        <LogOutButton />
    </div>
  </v-app-bar>
</template>

<script setup>
import LogOutButton from '~/components/features/auth/LogOutButton.vue'
defineEmits(['ask-ai'])
</script>

<style scoped>
.logo {
  font-size: 20px;
  font-weight: bold;
  text-decoration: none;
  color: #6D0037;
  white-space: nowrap;
  flex: 1;
}

.center {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 2;
  justify-content: center;
}

.links {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  justify-content: flex-end;
}

.links a {
  text-decoration: none;
  color: #333;
  font-size: 14px;
  white-space: nowrap;
}

.links a:hover,
.links a.router-link-active {
  color: #6D0037;
  font-weight: 600;
}
</style>
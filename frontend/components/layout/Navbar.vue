<template> 
    <nav class="navbar">

        <NuxtLink to="/" class="logo">
          <img src="/images/logo.png" alt="Boardwise" class="logo-img" />
        </NuxtLink>

        <div class="center">
            <div class="search-bar">
                <span class="search-icon">i</span>
                <input placeholder="Search games, users, rules..." />
            </div>

            <BaseButton class="ask-ai-btn" @click="$emit('ask-ai')">
                + Ask AI
            </BaseButton>
        </div>

        <div class="links">
            <NuxtLink to="/library">
                Library
            </NuxtLink> 

            <NuxtLink to="/marketplace">
                Marketplace
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
        </div>

    </nav>
</template>

<script setup>
defineEmits(['ask-ai'])

import BaseButton from '~/components/ui/BaseButton.vue'
</script>

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
  height: 40px;
  object-fit: contain;
}

.links {
  display: flex;
  gap: 20px;
  flex: 1;
  justify-content: center;
}

.links a {
  text-decoration: none;
  color: #333;
  white-space: nowrap;
  font-size: 14px;
}

.links a:hover {
  color: #6C3BFF;
}

.center {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 2;
  justify-content: center;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f5f5;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 8px 14px;
  min-width: 220px;
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: #333;
  width: 100%;
}

.search-icon {
  font-size: 14px;
  color: #888;
}

.ask-ai-btn {
  background: #6C3BFF;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.ask-ai-btn:hover {
  background: #5a2de0;
}

.right {
  flex: 1;
}
</style>
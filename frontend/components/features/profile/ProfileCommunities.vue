<template>
  <div class="mt-6">

    <div class="d-flex justify-space-between align-center mb-3">
      <h3 class="text-subtitle-1 font-weight-semibold ma-0">Communities</h3>
      <NuxtLink to="/community" class="text-primary text-decoration-none font-weight-bold" style="font-size: 20px;">›</NuxtLink>
    </div>

    <div class="cards-row">
      <NuxtLink
        v-for="community in communities"
        :key="community.id"
        :to="`/community/${community.id}`"
        class="community-card text-decoration-none"
      >
        <v-avatar size="64" rounded="lg" class="community-avatar">
          <v-img :src="community.image" :alt="community.name" cover />
        </v-avatar>
        <p class="card-name text-grey">{{ community.name }}</p>
      </NuxtLink>
    </div>

  </div>
</template>

<script setup>
import { communities } from '~/services/mockData/communities'
</script>

<style scoped>
.cards-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: none;
}

.cards-row::-webkit-scrollbar { display: none; }

.community-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  cursor: pointer;
}

.community-avatar {
  border: 2px solid #eee;
  transition: border-color 0.2s;
}

.community-card:hover .community-avatar {
  border-color: rgb(var(--v-theme-primary));
}

.card-name {
  font-size: 11px;
  text-align: center;
  margin: 0;
  max-width: 64px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
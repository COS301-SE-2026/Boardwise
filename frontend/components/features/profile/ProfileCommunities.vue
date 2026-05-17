<template>
  <div class="communities-section">

    <div class="header">
      <h3>Communities</h3>
      <NuxtLink to="/community" class="see-all">›</NuxtLink>
    </div>

    <div class="cards-row">
      <NuxtLink
        v-for="community in communities"
        :key="community.id"
        :to="`/community/${community.id}`"
        class="community-card"
      >
        <div class="card-image">
          <img :src="community.image" :alt="community.name" />
        </div>
        <p class="card-name">{{ community.name }}</p>
      </NuxtLink>
    </div>

  </div>
</template>

<script setup>
// Mock data — replace with real data later
const communities = [
  { id: 1, name: 'Strategy', image: '/images/catan.jpg' },
  { id: 2, name: 'Family', image: '/images/dixit.jpg' },
  { id: 3, name: 'Abstract', image: '/images/azul.jpg' },
  { id: 4, name: 'Get Together', image: '/images/catan.jpg' }
]
</script>

<style scoped>
.communities-section {
  margin-top: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.see-all {
  font-size: 20px;
  color: #6C3BFF;
  text-decoration: none;
  font-weight: bold;
}

.cards-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: none;
}

.cards-row::-webkit-scrollbar {
  display: none;
}

.community-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  flex-shrink: 0;
  cursor: pointer;
}

.card-image {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #eee;
  transition: border-color 0.2s;
}

.community-card:hover .card-image {
  border-color: #6C3BFF;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-name {
  font-size: 11px;
  color: #555;
  text-align: center;
  margin: 0;
  max-width: 64px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
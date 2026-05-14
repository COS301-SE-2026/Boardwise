<template>
  <BaseCard class="stats-card">

    <div class="stat">
      <h2>{{ games }}</h2>
      <p>Games</p>
    </div>

    <div class="divider" />

    <div class="stat">
      <h2>{{ friends }}</h2>
      <p>Friends</p>
    </div>

    <div class="divider" />

    <div class="stat">
      <h2>{{ communities }}</h2>
      <p>Communities</p>
    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  games: Number,
  friends: Number,
  communities: Number,
  reviews: Number
})
</script>

<style scoped>
.stats-card {
  display: flex;
  justify-content: space-around;
  align-items: center;

  padding: 24px;
  margin-top: -40px;
  position: relative;
  z-index: 2;
}

.stat {
  text-align: center;
  flex: 1;
}

.stat h2 {
  margin: 0;
  font-size: 32px;
}

.stat p {
  color: #666;
  margin-top: 8px;
}

.divider {
  width: 1px;
  height: 60px;
  background: #eee;
}

@media (max-width: 768px) {
  .stats-card {
    flex-wrap: wrap;
    gap: 24px;
  }

  .divider {
    display: none;
  }
}
</style>
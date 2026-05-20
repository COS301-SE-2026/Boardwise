<template>
  <div class="topbar">

    <NuxtLink :to="`/library/${rulebook?.id}`" class="back">
      ← Back
    </NuxtLink>

    <h2>{{ rulebook?.title }}</h2>

    <span class="page-indicator">
      {{ currentPage + 1 }} / {{ rulebook?.pages?.length }}
    </span>

  </div>
</template>

<script setup>
defineProps({
  rulebook: Object,
  currentPage: Number
})
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: white;
  padding: 16px 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  border-bottom: 1px solid #eee;
}

.back {
  text-decoration: none;
  color: #6C3BFF;
  font-weight: 600;
  white-space: nowrap;
}

.topbar h2 {
  margin: 0;
  font-size: 18px;
  flex: 1;
}

.page-indicator {
  font-size: 13px;
  color: #888;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .topbar { padding: 16px; }
  .topbar h2 { font-size: 15px; }
}
</style>
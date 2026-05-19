<template>
  <aside class="sidebar">

    <p class="sidebar-title">Contents</p>

    <div
      v-for="(page, index) in pages"
      :key="index"
      class="sidebar-item"
      :class="{ active: currentPage === index }"
      @click="$emit('change', index)"
    >
      <span class="index">{{ index + 1 }}</span>
      {{ page.title }}
    </div>

  </aside>
</template>

<script setup>
defineProps({
  pages: {
    type: Array,
    default: () => []
  },
  currentPage: Number
})

defineEmits(['change'])
</script>

<style scoped>
.sidebar {
  width: 200px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #eee;
  align-self: flex-start;
}

.sidebar-title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #aaa;
  margin: 0 0 12px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: 0.15s;
  margin-bottom: 4px;
}

.sidebar-item:hover {
  background: #f5f5f5;
  color: #333;
}

.sidebar-item.active {
  background: #f3eeff;
  color: #6C3BFF;
  font-weight: 600;
}

.index {
  font-size: 11px;
  background: #eee;
  color: #888;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 600;
}

.sidebar-item.active .index {
  background: #6C3BFF;
  color: white;
}

@media (max-width: 900px) {
  .sidebar {
    width: 100%;
    position: relative;
    top: unset;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 16px;
  }

  .sidebar-title {
    width: 100%;
    margin-bottom: 4px;
  }

  .sidebar-item {
    margin-bottom: 0;
  }
}
</style>
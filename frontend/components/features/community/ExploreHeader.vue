<template>
    <div class="explore-header">
    <div class="hero-bg" />
    <div class="hero-content">
      <SectionTitle title="Community" subtitle="Find your people" />
    </div>
  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
</script>

<style scoped>
.explore-header {
    position: relative;
    width: 100%;
    height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 60%, rgba(135, 20, 184, 0.5) 0%, transparent 55%),
    radial-gradient(ellipse at 78% 25%, rgba(249, 22, 135, 0.4) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 90%, rgba(255, 141, 59, 0.35) 0%, transparent 55%),
    linear-gradient(135deg, #0f172a 0%, #1a1f35 60%, #0f172a 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
}
 
.hero-content :deep(h2) {
  color: #ffffff;
  font-size: 2.5rem;
  margin: 0 0 8px;
}
 
.hero-content :deep(p) {
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}
</style>
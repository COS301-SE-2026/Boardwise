<template>
  <div class="reader-card">

    <img
      :src="rulebook?.image"
      :alt="rulebook?.title"
      class="banner"
    />

    <div class="content">

      <p class="category">{{ rulebook?.category }}</p>
      <h1>{{ rulebook?.title }}</h1>

      <div class="stats-row">
        <span> {{ rulebook?.players }} players</span>
        <span> {{ rulebook?.duration }}</span>
        <span> {{ rulebook?.difficulty }}</span>
        <span> Age {{ rulebook?.age }}</span>
      </div>

      <div class="divider" />

      <h2>{{ page?.title }}</h2>
      <p class="body-text">{{ page?.content }}</p>

      <div class="nav-buttons">
        <BaseButton
          variant="secondary"
          :disabled="isFirst"
          @click="$emit('prev')"
        >
          ← Previous
        </BaseButton>

        <BaseButton
          :disabled="isLast"
          @click="$emit('next')"
        >
          Next →
        </BaseButton>
      </div>

    </div>

  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  rulebook: Object,
  page: Object,
  isFirst: Boolean,
  isLast: Boolean
})

defineEmits(['prev', 'next'])
</script>

<style scoped>
.reader-card {
  background: white;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
}

.banner {
  width: 100%;
  height: 280px;
  object-fit: cover;
}

.content { padding: 40px; }

.category {
  color: #6C3BFF;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0 0 8px;
}

h1 { margin: 0 0 16px; font-size: 40px; line-height: 1.1; }

.stats-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #666;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 28px 0;
}

h2 { margin: 0 0 16px; font-size: 24px; color: #1e1e1e; }

.body-text {
  color: #444;
  font-size: 16px;
  line-height: 1.9;
  margin: 0;
}

.nav-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .content { padding: 24px; }
  h1 { font-size: 28px; }
  .banner { height: 200px; }
}
</style>
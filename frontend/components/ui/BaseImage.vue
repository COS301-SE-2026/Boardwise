<template>
  <div class="image-wrapper" :style="{ height: height, width: width }">
    <img
      v-if="!error"
      :src="src"
      :alt="alt"
      :class="['image', { cover: fit === 'cover', contain: fit === 'contain' }]"
      @error="error = true"
    />
    <div v-else class="placeholder">
      <span>{{ alt ?? 'Image' }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  src: String,
  alt: String,
  height: {
    type: String,
    default: '200px'
  },
  width: {
    type: String,
    default: '100%'
  },
  fit: {
    type: String,
    default: 'cover'
    // 'cover' | 'contain'
  }
})

const error = ref(false)
</script>

<style scoped>
.image-wrapper {
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 8px;
}

.image {
  width: 100%;
  height: 100%;
}

.cover { object-fit: cover; }
.contain { object-fit: contain; }

.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 13px;
  background: #f0f0f0;
}
</style>
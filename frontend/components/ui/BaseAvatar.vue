<template>
  <v-avatar :size="sizeMap[size] || sizeMap.md" clas="base-avatar">
    <v-img 
      v-if="src && !imageError"
      :src="src"
      :alt="resolvedAlt"
      cover
      @error="imageError = true"
    ></v-img>

    <span
      v-else
      class="base-avatar__fallback"
      role="img"
      :aria-label="fallbackLabel"
    >
      {{ initials }}
    </span>
  </v-avatar>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  }, 
  alt: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md',
  },
  name: {
    type: String,
    default: ''
  }
})

const imageError = ref(false)

const sizeMap = {
  sm: '32',
  md: '48',
  lg: '64',
  xl: '96',}

const initials = computed(() => {
  if (!props.name) return '?'
  return props.name.trim().split(/\s+/).filter(Boolean).map(w => w[0]).join('').toUpperCase().slice(0, 2)
})

const resolvedAlt = computed(() => {
  if (props.alt !== undefined) {
    return props.alt
  }

  return props.name
    ? `${props.name} profile picture`
    : ''
})

const fallbackLabel = computed(() => {
  return props.name
    ? `${props.name} profile picture`
    : 'User profile picture'
})
</script>

<style scoped>
.avatar-intials {
  font-weight: var(--fw-bold);
  color: var(--color-text-muted);
}
</style>
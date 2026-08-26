<template>
  <v-avatar :size="sizeMap[size]">
    <v-img v-if="src" :src="src" :alt="alt ?? name" />
    <span v-else class="avatar-intials">{{  initials }}</span>
  </v-avatar>
</template>

<script setup>
const props = defineProps({
  src: String, 
  alt: String,
  size: {
    type: String,
    default: 'md',
  },
  name: String
})

const sizeMap = {
  sm: '32',
  md: '48',
  lg: '64',
  xl: '96',}

const initials = computed(() => {
  if (!props.name) return '?'
  return props.name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
})
</script>

<style scoped>
.avatar-intials {
  font-weight: var(--fw-bold);
  color: var(--color-text-muted);
}
</style>
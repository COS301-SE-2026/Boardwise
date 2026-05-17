<template>
  <div class="dropdown">

    <BaseButton @click="open = !open">
      {{ label }}
    </BaseButton>

    <div v-if="open" class="menu">
      <slot />
    </div>

  </div>
</template>

<script setup>
import BaseButton from './BaseButton.vue'

defineProps({
  label: String
})

const open = ref(false)
const dropdownRef = ref(null)

const handleClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    open.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.dropdown {
  position: relative;
  display: inline-block;
}

.menu {
  position: absolute;
  top: 110%;
  left: 0;
  z-index: 100;
  min-width: 160px;
  background: white;

  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
</style>
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
</script>

<style scoped>
.dropdown {
  position: relative;
}

.menu {
  position: absolute;
  top: 110%;
  left: 0;

  background: white;

  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
}
</style>
<template>
  <div class="tabs">

    <BaseButton
      v-for="tab in tabs"
      :key="tab"
      @click="$emit('change', tab)"
      :class="{ active: tab === activeTab }"
    >
      {{ tab }}
    </BaseButton>

  </div>
</template>

<script setup>
import BaseButton from './BaseButton.vue'

defineProps({
  tabs: Array,
  activeTab: String
})

defineEmits(['change'])
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 12px;
}

.active {
  background: #4E2ACF;
}
</style>
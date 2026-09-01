<template>
  <v-btn-toggle
    :model-value="activeTab"
    :aria-label="ariaLabel"
    class="base-tag-group"
    @update:model-value="$emit('change', $event)"
  >
    <v-btn
      v-for="tab in tabs"
      :key="tab"
      :value="tab"
    >
      {{ tab }}
    </v-btn>
  </v-btn-toggle>
</template>

<script setup>
defineProps({
  tabs: {
    type: Array,
    default: () => []
  },
  activeTab: {
    type: String,
    default: ''
  },
  ariaLabel: {
    type: String,
    default: 'Options'
  }
})

defineEmits(['change'])
</script>
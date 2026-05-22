<template>
  <v-text-field
    :model-value="search"
    placeholder="placeholder"
    variant="outlined"
    @update:model-value="search = $event"
  />
</template>

<script setup>
defineProps({
  placeholder: String
})

const search = defineModel()
</script>
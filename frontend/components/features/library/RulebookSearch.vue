<template>
  <div class="d-flex gap-3 align-center">
    <BaseSearch
      v-model="search"
      placeholder="Search rulebooks..."
      @update:model-value="$emit('search', $event)"
    />
    <BaseButton variant="primary" @click="$emit('upload')">
      <v-icon start>mdi-upload</v-icon>
      Upload Rulebook
    </BaseButton>
  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')
defineEmits(['search', 'upload'])
</script>
<template>
  <div class="d-flex flex-column ga-1">
    <h2 class="text-h5 font-weight-bold mb-1">
      {{ rulebook.title }}
    </h2>

    <p class="text-body-2 text-medium-emphasis mb-0">
      Author: {{ rulebook.author || 'Unknown' }}
    </p>

    <p class="text-body-2 text-medium-emphasis mb-0">
      Date of Issue: {{ formattedDate }}
    </p>

  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})

const formattedDate = computed(() => {
  return props.rulebook.uploadedAt ? new Date(props.rulebook.uploadedAt).toLocaleDateString() : 'Unknown'
})
</script>
<template>
  <BaseCard class="pa-7">
    <h3 class="text-h6 font-weight-bold mb-4">
      Description
    </h3>

    <p class="text-medium-emphasis mb-0" style="line-height: 1.8;">
      {{ rulebook.description }}
    </p>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})

</script>

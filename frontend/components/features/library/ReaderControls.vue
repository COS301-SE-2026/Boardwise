<template>
  <div class="controls">

    <BaseButton>
      Previous
    </BaseButton>

    <BaseButton>
      Next
    </BaseButton>

  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
</script>

<style scoped>
.controls {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
</style>
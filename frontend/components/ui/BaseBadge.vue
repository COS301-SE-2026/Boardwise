<template>
  <span class="badge"
    :class="variant"
  >
    <slot />
  </span>
</template>

<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: 'default'
  }
})

</script>

<style scoped>
.badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.rent {
  background: #6c3bff;
  color: white;
}

.sale {
  background: #F59E0B;
  color: white;
}

.warning {
  background: #fef9c3;
  color: #ca8a04;
}

.danger {
  background: #fee2e2;
  color: #dc2626;
}
</style>
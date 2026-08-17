<template>
  <BaseGrid>
    <RetailerCard
      v-for="retail in retailers"
      :key="retail.url"
      :retail="retail"
    />
  </BaseGrid>
</template>

<script setup>
import BaseGrid    from '~/components/ui/BaseGrid.vue'
import RetailerCard from './RetailerCard.vue'

defineProps({
  retailers: { type: Array, default: () => [] }
})
</script>

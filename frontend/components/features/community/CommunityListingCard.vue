<template>
  <BaseCard
    class="cursor-pointer"
    @click="openListing"
  >

  <BaseImage
    :src="listing.image"
    :alt="listing.name"
    height="220px"
    fit="contain"
  />

    <div class="pa-4 d-flex flex-column ga-2">

    <h3 class="text-h6">
      {{ listing.name }}
    </h3>

    <div class="d-flex justify-space-between">

      <span class="text-primary font-weight-bold">
        {{ listing.price }}
      </span>

      <span class="text-body-2 text-medium-emphasis">
        {{ listing.location }}
      </span>

    </div>
  </div>

  </BaseCard>
</template>

<script setup>
import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue';

const props = defineProps({
  listing: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const openListing = () => {
  router.push(`/marketplace/${props.listing.id}`)
}
</script>

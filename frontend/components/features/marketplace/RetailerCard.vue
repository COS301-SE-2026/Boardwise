<template>
  <BaseCard data-test="retailer-card" clickable @click="openRetailLink">

    <template #media>
      <BaseImage
        data-test="retailer-image"
        :src="retail.imageUrl ?? '/default-listing.png'"
        :alt="retail.retailTitle"
        height="200px"
      />
    </template>

    <p class="card-title" data-test="retailer-title">
      {{ retail.retailTitle }}
    </p>

    <p class="card-subtitle" data-test="retailer-name">
      {{ retail.retailer }}
    </p>

    <p
      v-if="retail.price != null"
      class="price ma-0"
      data-test="retailer-price"
    >
      R{{ retail.price }} 
    </p>

  </BaseCard>
</template>

<script setup>
import BaseCard  from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
  retail: { type: Object, required: true }
})

const router = useRouter()

const openRetailLink = () => {
  if(!props.retail.url) return

  window.open(
    props.retail.url,
    '_blank',
    'noopener,noreferrer'
  )
}
</script>

<style scoped>
.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}
</style>
<template>
  <BaseCard class="retail-card" @click="openRetailLink">

    <div class="image-container">
      <BaseImage
        :src="retail.imageUrl ?? '/default-listing.png'"
        :alt="retail.retailTitle"
        height="200px"
      />
    </div>

    <v-card-text class="pa-4 d-flex flex-column ga-2">

      <h2 class="retail-title">
        {{ retailers.retailerTitle }}
      </h2>

      <h3 class="retail-name">
        {{ retailers.retailerName }}
      </h3>

      <p
        class="price ma-0"
      >
        R{{ retailer.price }} 
        <br />
      </p>
    </v-card-text>

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
.retail-card {
  cursor: pointer;
}

.retail-card:hover {
  transform:  translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.image-container {
  position: relative;
}

.retail-title {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  color: var(--color-primary);
  line-height: 1.2;
  margin: 0;
}

.retailer-name {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-semibold);
}

.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}
</style>
<template>
  <BaseCard>

    <div class="listing-card" @click="openListing">

      <div class="image-container">
        <BaseImage :src="listing.image" :alt="listing.title" height="200px" />
        <BaseBadge :variant="listing.type === 'rent' ? 'rent' : 'sale'" class="badge">
          {{ listing.type === 'rent' ? 'For rent' : 'For sale' }}
        </BaseBadge>
      </div>

      <div class="content">
        <h3>{{ listing.title }}</h3>
        <p class="price">
          R{{ listing.price }}
          <span v-if="listing.type === 'rent'" class="period">/ {{ listing.rentalPeriod ?? 'week' }}</span>
        </p>

        <div class="meta">
            <span class="seller">@{{ listing.seller ?? 'unknown' }}</span>
            <span class="location"> {{ listing.location }}</span>
        </div>

      </div>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
  listing: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const openListing = () => {
  router.push(`/specific-item/${props.listing.id}`)
}
</script>

<style scoped>
.listing-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
}

.image-container {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.badge {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
}

.price {
  color: var(--color-primary);
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
  margin: 0;
}

.period {
  font-size: var(--fs-small);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.content {
  padding: var(--space-3) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

h3 {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>
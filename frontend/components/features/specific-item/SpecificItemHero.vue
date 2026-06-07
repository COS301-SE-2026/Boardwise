<template>
  <div class="hero">

    <div class="image-container">
      <BaseImage :src="listing.imageUrl" :alt="listing.listingTitle" height="400px"/>
      <BaseBadge :variant="listing.listingType === 'rental' ? 'rent' : 'sale'" class="badge">
        {{ listing.listingType === 'rental' ? 'For rent' : 'For sale' }}
      </BaseBadge>
    </div>

    <div class="info">

      <h1>{{ listing.listingTitle }}</h1>

      <p class="price">
        R{{ listing.price }}
        <span v-if="listing.listingType === 'rental' && rentalDates" class="period">
          / {{ rentalDates }}
        </span>
      </p>

      <div class="meta">
        <span>@{{ listing.username ?? 'unknown' }}</span>
        <span>{{ listing.location }}</span>
      </div>

      <p class="description">{{ listing.description ?? 'No description provided.' }}</p>

      <div v-if="listing.isNegotiable" class="negotiable">
        Open to negotiation
      </div>

      <div class="actions">
        <BaseButton @click="showContact = true">Contact lister</BaseButton>
        <BaseButton variant="secondary" @click="$router.push(`/library/${listing.rulebookId}`)">
            Read rulebook
        </BaseButton>
        <BaseButton variant="secondary" @click="$router.back()">Go back</BaseButton>
        </div>

    </div>
    <ContactListerModal
      v-model="showContact"
      :listing-title="listing.listingTitle"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import ContactListerModal from './ContactListerModal.vue'

defineProps({
  listing: {
    type: Object,
    required: true
  }
})

const showContact = ref(false)

const rentalDates = computed(() => {
  const period = props.listing.rentalPeriod
  if (!Array.isArray(period) || period.length < 2) return null
  return `${period[0]} to ${period[1]}`
})
</script>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-10);
}

.image-container {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.badge {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
}

.info {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

h1 {
  margin: 0;
  font-size: var(--fs-h1);
  color: var(--color-secondary);
}

.price {
  font-size: 28px;
  font-weight: var(--fw-bold);
  color: var(--color-primary);
  margin: 0;
}

.period {
  font-size: var(--fs-body);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.meta {
  display: flex;
  gap: var(--space-4);
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.description {
  color: var(--color-text);
  line-height: var(--lh-relaxed);
  margin: 0;
}

.negotiable {
  font-size: var(--fs-small);
  color: var(--color-success);
  font-weight: var(--fw-bold);
}

.actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}
</style>
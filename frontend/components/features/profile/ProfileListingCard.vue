<template>
  <BaseCard class="profile-listing-card">

    <div class="image-container">
      <img
        :src="listing.imageUrl ?? '/listing-detail.png'"
        :alt="listing.gameTitle"
      />
      <BaseBadge
        class="badge"
        :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
      >
        {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
      </BaseBadge>
    </div>

    <v-card-text class="d-flex flex-column ga-2 pa-4">
      <h3>{{ listing.gameTitle }}</h3>

      <p
        class="price ma-0"
        :style="{ color: listing.listingType === 'rental' ? 'var(--rent)' : 'var(--sale)' }"
      >
        R{{ listing.price }}
        <span v-if="listing.listingType === 'rental'" class="period">
          {{
            listing.rentalPeriod
              ? `(${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate})`
              : 'week'
          }}
        </span>
      </p>

      <div class="meta">
        <span>@{{ listing.username ?? 'unknown' }}</span>
      </div>

      <div class="d-flex ga-2 mt-1">
        <v-btn size="small" color="primary" variant="tonal" @click.stop="showEdit = true">Edit</v-btn>
        <v-btn size="small" color="error"   variant="tonal" @click.stop="showDelete = true">Delete</v-btn>
      </div>
    </v-card-text>

    <EditListingModal   v-model="showEdit"   :listing="listing" />
    <DeleteListingModal v-model="showDelete" :listing="listing" @confirm="handleDelete"  />

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import EditListingModal from './EditListingModal.vue'
import DeleteListingModal from './DeleteListingModal.vue'
import { deleteListing } from '~/services/marketplaceService.js'

const props = defineProps({
  listing: { type: Object, required: true }
})

const router = useRouter()
const showEdit   = ref(false)
const showDelete = ref(false)

const openListing = () => {
  router.push(`/marketplace/${props.listing.listingId}`)
}
const handleDelete = async () => {
  await deleteListing(props.listing.listingId);
  emit('deleted', props.listing.listingId)
};

const emit = defineEmits(['deleted']);
</script>

<style scoped>
.profile-listing-card {
  cursor:     pointer;
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  width: 100%;
}
:deep(.badge) {
  color: white;
}

.profile-listing-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: var(--color-surface-alt);
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  top:  var(--space-2);
  left: var(--space-2);
}

h3 {
  margin: 0;
  font-size: var(--fs-body);
}

.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}

.period {
  font-size: var(--fs-small);
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
}

.meta {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>
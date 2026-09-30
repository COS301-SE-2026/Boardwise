<template>
  <BaseCard class="profile-listing-card" data-test="profile-listing-card" clickable @click="openListing">

    <template #media>
      <div class="listing-listing-card_media">
        <BaseImage
          :src="listing.imageUrl ?? '/images/default-listing.png'"
          :alt="listing.gameTitle"
          height="200px"
        />

        <BaseBadge
          class="badge--absolute"
          :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
        >
          {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
        </BaseBadge>
      </div>
    </template>
    
    <div class="profile-listing-card-content">
      <p class="card-title">{{ listing.gameTitle }}</p>

      <p class="listing-price">
        R {{  listing.price  }}
      </p>

      <p
        v-if="listing.listingType === 'rental'"
        class="listing-period"
      >
        {{
          listing.rentalPeriod
            ? `(${listing.rentalPeriod.startDate} – ${listing.rentalPeriod.endDate})`
            : 'Rental'
        }}
      </p>

      <div v-if="editable" class="listing-actions">
        <v-btn size="small" color="primary" variant="tonal" @click.stop="showEdit = true">Edit</v-btn>
        <v-btn size="small" color="error"   variant="tonal" @click.stop="showDelete = true">Delete</v-btn>
      </div>
    </div>

    <EditListingModal v-model="showEdit" :listing="listing" @saved="$emit('updated', listing.listingId)"  />
    <DeleteListingModal v-model="showDelete" :listing="listing" @confirm="handleDelete"  />

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

import EditListingModal from './EditListingModal.vue'
import DeleteListingModal from './DeleteListingModal.vue'

import { useMarketplace } from '~/composables/useMarketplace'

const { removeListing } = useMarketplace();

const props = defineProps({
  listing: { type: Object, required: true },
  editable: { type: Boolean, default: false }
})

const router = useRouter()
const showEdit   = ref(false)
const showDelete = ref(false)

const openListing = () => {
  router.push(`/marketplace/${props.listing.listingId}`)
}

const handleDelete = async () => {
  try {
    await removeListing(props.listing.listingId)
    emit('deleted', props.listing.listingId)
  } catch (error) {
    console.error('Failed to delete listing:', error)
  }
    
};

const emit = defineEmits(['deleted','updated']);

</script>

<style scoped>
.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}

.price--rent {
  color: var(--rent);
}

.price--sale {
  color: var(--sale);
}

.period {
  font-size: var(--fs-small);
  font-weight: var(--fw-regular);
}
</style>
<template>
  <BaseCard data-test="profile-listing-card" clickable @click="openListing">

    <template #media>
      <BaseImage
        :src="listing.imageUrl ?? '/images/default-listing.png'"
        :alt="listing.gameTitle"
        height="200px"
      />

      <BaseBadge
        class="badge"
        :variant="listing.listingType === 'rental' ? 'rent' : 'sale'"
      >
        {{ listing.listingType === 'rental' ? 'For Rent' : 'For Sale' }}
      </BaseBadge>
    </template>

    
    <p class="card-title">{{ listing.gameTitle }}</p>

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

    <template v-if="editable" #actions>
      <BaseButton size="sm" @click.stop="showEdit = true">Edit</BaseButton>
      <BaseButton size="sm" variant="secondary" @click.stop="showDelete = true">Delete</BaseButton>
    </template>

    <EditListingModal   v-model="showEdit"   :listing="listing" @saved="$emit('updated', listing.listingId)"  />
    <DeleteListingModal v-model="showDelete" :listing="listing" @confirm="handleDelete"  />

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

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
  await removeListing(props.listing.listingId)
  emit('deleted', props.listing.listingId)
};

const emit = defineEmits(['deleted','updated']);

</script>

<style scoped>

.price {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
}
</style>
<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6 d-flex flex-column ga-5" style="background: var(--color-surface) !important; overflow-y: auto;">

      <h2 class="ma-0">Edit Listing</h2>

      <v-text-field v-model="listing_title" label="Listing Title" placeholder="Listing title" variant="outlined" density="compact" hide-details />

      <v-text-field v-model="game_title" label="Game Title" placeholder="Game title" variant ="outlined" density="compact" hide-details/>

      <v-text-field v-model="version" label="Version" placeholder="e.g. Original" variant ="outlined" density="compact" hide-details/>

      <v-autocomplete
        v-model="selected_genres"
        label="Genres"
        :items="genres"
        :loading="genresLoading"
        multiple
        chips
        closable-chips
        variant="outlined"
        density="compact"
        hide-details
        @update:search="onGenreSearch"
      />

      <v-select v-model="selected_condition" 
      label="Condition"
      :items="conditions">
    </v-select>

      <v-select v-model="selected_item_type" 
      label="Item Type"
      :items="item_types">
    </v-select>

     <div class="d-flex">
        <v-btn-toggle v-model="listing_type" color="primary" variant="outlined" mandatory divided>
          <v-btn value="sell">Sell</v-btn>
          <v-btn value="rent">Rent</v-btn>
        </v-btn-toggle>
      </div>


      <v-text-field  v-model="price" label="Amount" prefix="R" placeholder="e.g. 650" type="number" variant="outlined" density="compact" hide-details />

      <div class="RentalPeriod">
        <div v-if="listing_type === 'rent'">
            <v-date-input v-model ="start_date" label="Start Date" variant="outlined"></v-date-input>
            <v-date-input  v-model ="end_date" label="End Date" variant="outlined"></v-date-input>
        </div>
        <div v-else>
              <v-checkbox v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />
        </div>
      </div>


      <v-text-field v-model="location" label="Location" placeholder="e.g. Pretoria" variant="outlined" density="compact" hide-details />

      <v-textarea v-model="description" label="Description" placeholder="description" variant ="outlined" density="compact" hide-details/>

      <div class="d-flex align-center ga-3">
        <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Image</v-btn>
        <label for="edit-image-upload" class="text-grey text-body-2">{{ file_name || '···' }}</label>
        <input id="edit-image-upload" ref="file_input" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleSave">Edit Listing</v-btn>
      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>
import { useMarketplace } from '~/composables/useMarketplace'
import { useBoardGames } from '~/composables/useBoardGames'
const { editListing } = useMarketplace()

const { searchGenres, genres, isLoading: genresLoading } = useBoardGames()

onMounted(() => searchGenres())

let genreSearchTimeout
const onGenreSearch = (query) => {
  clearTimeout(genreSearchTimeout)
  genreSearchTimeout = setTimeout(() => searchGenres(query), 300)
}

const open  = defineModel()
const props = defineProps({ listing: Object })
const emit  = defineEmits(['saved'])

const listing_title = ref('')
const game_title = ref('')
const description = ref('')
const listing_type = ref('sell')
const rental_period = ref('')
const negotiable = ref(false)
const price = ref(0)
const location= ref('')
const file_name = ref('')
const file_input= ref(null)
const image_file = ref(null)

const version = ref('');

const selected_genres = ref([])
const selected_condition = ref(null)
const selected_item_type = ref(null)

const start_date = ref(null);
const end_date = ref(null);

watch(open, val =>{//listen for an open & populate ref
  if(!val||!props.listing) return;
  const listing_element = props.listing
  listing_title.value = listing_element.listingTitle ?? ''
  game_title.value = listing_element.gameTitle ?? ''
  description.value= listing_element.description ?? ''
  listing_type.value = listing_element.listingType === 'rental' ? 'rent' : 'sell'
  negotiable.value = listing_element.isNegotiable ?? false
  price.value = listing_element.price ?? 0
  location.value = listing_element.location ?? ''
  version.value = listing_element.version ?? ''
  selected_genres.value = listing_element.genres ?? []
  selected_condition.value = listing_element.condition ?? null
  selected_item_type.value = listing_element.itemType ?? null
  start_date.value = listing_element.rentalPeriod?.startDate ?? null
  end_date.value = listing_element.rentalPeriod?.endDate?? null
  
  if (selected_genres.value.length) {
      const missing = selected_genres.value.filter(g => !genres.value.includes(g))
      if (missing.length) genres.value = [...missing, ...genres.value]
    }

});

const triggerUpload = () => file_input.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0] ?? null
  image_file.value = file
  file_name.value  = file?.name ?? null
} 

function get_rental_period() {
  const fmt = (d) => {// reformat data
    const date = new Date(d)// date
    const y = date.getFullYear()// this year
    const m = String(date.getMonth() + 1).padStart(2, '0')//format as MM
    const day = String(date.getDate()).padStart(2, '0') // format as DD
    return `${y}-${m}-${day}`
  }
  return [fmt(start_date.value), fmt(end_date.value)]
}

const handleSave = async () => {
  const listingData = {
    listingTitle: listing_title.value,
    gameTitle: game_title.value,
    listingType:(listing_type.value === 'rent')?'rental':'sale',
    itemType: selected_item_type.value,
    description: description.value,
    price: Number(price.value),
    condition: selected_condition.value,
    isNegotiable: negotiable.value,
    rentalPeriod: (listing_type.value === 'rent')? get_rental_period():null,
    genres: selected_genres.value,
    version: version.value,
    location: location.value
  }

  
  await editListing(props.listing.listingId, listingData, image_file.value ?? undefined)
  emit('saved', 'updated')
  open.value = false
}

const closeModal = () => {
  open.value = false
  listing_title.value = ''
  game_title.value =''
  version.value = ''
  description.value = ''
  selected_condition.value=''
  selected_genres.value =[]
  selected_item_type.value=''
  listing_type.value = 'sell'
  price.value = ''
  rental_period.value = ''
  negotiable.value = false
  location.value = ''
  file_name.value = ''
  image_file.value = null
}
const conditions = ['New', 'Like New', 'Good', 'Fair']

const item_types  = ["Merch", "Full Boardgame","Partial Boardgame","Pieces"]

</script>

<style scoped>
.hidden-input { display: none; }
</style>
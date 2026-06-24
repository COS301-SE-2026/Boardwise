<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6 d-flex flex-column ga-5" style="background: var(--color-surface) !important; overflow-y: auto;">
      <h2>Create Listing</h2>

      <v-text-field v-model="listing_title" label="Listing Title" placeholder="Listing title" variant="outlined" density="compact" hide-details />

      <v-text-field v-model="game_title" label="Game Title" placeholder="Game title" variant ="outlined" density="compact" hide-details/>

      <v-text-field v-model="version" label="Version" placeholder="e.g. Original" variant ="outlined" density="compact" hide-details/>

      <!--TODO: GET ALL AVAILABLE GENRES -->
      <v-select v-model="selected_genres"
        label="Genres"
        :items="genre_list"
        multiple
        chips
      ></v-select>

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
        <span class="text-grey text-body-2">{{ file_name || '···' }}</span>
        <input ref="file_input" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleConfirm">Create Listing</v-btn>
      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>

const isLoading = ref(false);

const open = defineModel()
const emit = defineEmits(['confirm'])

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
const file= ref(null);
const version = ref('');

const selected_genres = ref([])
const selected_condition = ref(null)
const selected_item_type = ref(null)

const start_date = ref(null);
const end_date = ref(null);

const triggerUpload = () => file_input.value.click()

const handleFileChange = (e) => {
  const toUpload = e.target.files[0]
  if (toUpload) {
    file_name.value = toUpload.name
    file.value = toUpload
  }
}
function getValidGenres(){
  return selected_genres.value.map(g => g.toLowerCase())
}

const closeModal = () => {
  isLoading.value = false;
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
  file.value = null
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

function get_valid_item_type(){
  return selected_item_type.value.toLowerCase()
}

function get_valid_condition(){
  return selected_condition.value.toLowerCase()
}


const handleConfirm = () => {
  if (!selected_item_type.value|| !game_title.value || !listing_title.value || !location.value ||!version.value ||
      !description.value || !selected_item_type.value || price.value < 0 || !selected_condition.value|| !description.value || 
      !selected_genres.value || selected_genres.value.length < 1){
        isLoading.value = false;
        return;
  }

  if(listing_type.value === 'rent' ){
    if(!start_date.value || !end_date.value){
      return;
    }
    // date validation
    const today = new Date()
    today.setHours(0, 0, 0, 0)

    const a = new Date(start_date.value)
    a.setHours(0, 0, 0, 0)

    const b = new Date(end_date.value)
    b.setHours(0, 0, 0, 0)

    //start is before today

    if (a < today) return

    //end date is before today
    if (b < today) return
  }

  isLoading.value = true;

  try{
    emit('confirm', {
      listingTitle: listing_title.value,
      gameTitle: game_title.value,
      listingType: listing_type.value === 'rent' ? 'rental' : 'sale', 
      price: Number(price.value),
      itemType: get_valid_item_type(),
      condition:get_valid_condition(),
      version: version.value,
      location: location.value,
      description: description.value,
      genres: getValidGenres(),
      isNegotiable: negotiable.value,
      rentalPeriod: listing_type.value === 'rent' ? get_rental_period() : null,
    }, file.value)

    closeModal();
  }
  finally{
    isLoading.value = false
  }
}

const conditions = ['New', 'Like New', 'Good', 'Fair']

const item_types  = ["Merch", "Full Boardgame","Partial Boardgame","Pieces"]

const genre_list = ['Strategy', 'Family', 'Adventure', 'Abstract', 'Party', 'Abstract Strategy','Card Game', 'Dice', 'Economic', 'Fantasy','Fighting','Electronic', 'Environmental', 'Horror', 'Humor', 'Mafia']
</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>
<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6 d-flex flex-column ga-5" style="background: var(--color-surface) !important; overflow-y: auto;">
      <h2>Create Listing</h2>

      <v-text-field v-model="listingTitle" label="Listing Title" placeholder="Listing title" variant="outlined" density="compact" hide-details />

      <v-text-field v-model="gameTitle" label="Game Title" placeholder="Game title" variant ="outlined" density="compact" hide-details/>

      <v-text-field v-model="version" label="Version" placeholder="e.g. Original" variant ="outlined" density="compact" hide-details/>

      <v-select v-model="selectedGenres"
        label="Genres"
        :items="genreList"
        multiple
        chips
      ></v-select>

      <v-select v-model="selectedCondition" 
      label="Condition"
      :items="conditions">
    </v-select>

      <v-select v-model="selectedItemType" 
      label="Item Type"
      :items="itemTypes">
    </v-select>

     <div class="d-flex">
        <v-btn-toggle v-model="listingType" color="primary" variant="outlined" mandatory divided>
          <v-btn value="sell">Sell</v-btn>
          <v-btn value="rent">Rent</v-btn>
        </v-btn-toggle>
      </div>


      <v-text-field  v-model="price" label="Amount" prefix="R" placeholder="e.g. 650" type="number" variant="outlined" density="compact" hide-details />

      <div class="RentalPeriod">
        <div v-if="listingType === 'rent'">
            <v-date-input v-model ="startDate" label="Start Date" variant="outlined"></v-date-input>
            <v-date-input  v-model ="endDate" label="End Date" variant="outlined"></v-date-input>
        </div>
        <div v-else>
              <v-checkbox v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />
        </div>
      </div>


      <div class="location">
        <v-switch label="Use current location" v-model="useCurrLocation" />
        <v-text-field
          v-model="location"
          label="Location"
          placeholder="e.g. Pretoria"
          variant="outlined"
          density="compact"
          hide-details
          :loading="useCurrLocation && loading"
          :readonly="useCurrLocation && loading"
        />
        <p v-if="useCurrLocation && locationError" class="text-error text-caption mt-1">
          Couldn't get your location, please enter it manually.
        </p>
      </div>

      <v-textarea v-model="description" label="Description" placeholder="description" variant ="outlined" density="compact" hide-details/>

      <div class="d-flex align-center ga-3">
        <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Image</v-btn>
        <label for="image-upload" class="text-grey text-body-2">{{ fileName || '···' }}</label>
        <input id="image-upload" ref="file_input" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleConfirm" :loading="isLoading">Create Listing</v-btn>      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>
import { useUserLocation } from '@/composables/useUserLocation';

const { city, suburb, lat, long, error: locationError, loading, findUserLocation } = useUserLocation();

const isLoading = ref(false);

const open =  defineModel({
  type: Boolean,
  default: false,
});
const emit = defineEmits(['confirm']);

const listingTitle = ref('');
const gameTitle = ref('');
const description = ref('');
const listingType = ref('sell');
const negotiable = ref(false);
const price = ref(0);
const location= ref('');
const fileName = ref('');
const file_input= ref(null);
const file= ref(null);
const version = ref('');
const useCurrLocation = ref(false);

const selectedGenres = ref([]);
const selectedCondition = ref(null);
const selectedItemType = ref(null);

const startDate = ref(null);
const endDate = ref(null);

const locationValue = computed(() =>
  [city.value, suburb.value].filter(Boolean).join(', ')
);

watch(useCurrLocation, async (val) => {
  if (!val) return;
  await findUserLocation();
  if (locationError.value) {
    useCurrLocation.value = false;
    return;
  }
  location.value = locationValue.value;
});

const triggerUpload = () => file_input.value.click();

const handleFileChange = (e) => {
  const toUpload = e.target.files[0];
  if (toUpload) {
    fileName.value = toUpload.name;
    file.value = toUpload;
  }
}
function getValidGenres(){
  return selectedGenres.value.map(g => g.toLowerCase());
}

const closeModal = () => {
  isLoading.value = false;
  open.value = false;
  listingTitle.value = '';
  gameTitle.value ='';
  version.value = '';
  description.value = '';
  selectedCondition.value='';
  selectedGenres.value =[];
  selectedItemType.value='';
  listingType.value = 'sell';
  price.value = '';
  negotiable.value = false;
  location.value = '';
  fileName.value = '';
  file.value = null;
  useCurrLocation.value = false;
}


function getRentalPeriod() {
  const fmt = (d) => {// reformat data
    const date = new Date(d);// date
    const y = date.getFullYear();// this year
    const m = String(date.getMonth() + 1).padStart(2, '0');//format as MM
    const day = String(date.getDate()).padStart(2, '0'); // format as DD
    return `${y}-${m}-${day}`;
  }
  return [fmt(startDate.value), fmt(endDate.value)];
}

function getValidItemType(){
  return selectedItemType.value.toLowerCase();
}

function getValidCondition(){
  return selectedCondition.value.toLowerCase();
}


const handleConfirm = () => {
  if (!selectedItemType.value|| !gameTitle.value || !listingTitle.value || !location.value ||!version.value ||
      price.value < 0 || !selectedCondition.value|| !description.value || !selectedGenres.value){
        isLoading.value = false;
        return;
  }

  if(listingType.value === 'rent' ){
    if(!startDate.value || !endDate.value){
      return;
    }
    // date validation
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const a = new Date(startDate.value);
    a.setHours(0, 0, 0, 0);

    const b = new Date(endDate.value);
    b.setHours(0, 0, 0, 0);

    //start is before today

    if (a < today) return;

    //end date is before today
    if (b < today) return;
  }

  isLoading.value = true;

  try{
    emit('confirm', {
      listingTitle: listingTitle.value,
      gameTitle: gameTitle.value,
      listingType: listingType.value === 'rent' ? 'rental' : 'sale', 
      price: Number(price.value),
      itemType: getValidItemType(),
      condition:getValidCondition(),
      version: version.value,
      location: location.value,
      description: description.value,
      genres: getValidGenres(),
      isNegotiable: negotiable.value,
      rentalPeriod: listingType.value === 'rent' ? getRentalPeriod() : null,
    }, file.value)

    closeModal();
  }
  finally{
    isLoading.value = false
  }
}

const conditions = ['New', 'Like New', 'Good', 'Fair'];

const itemTypes  = ["Merch", "Full Boardgame","Partial Boardgame","Pieces"];

const genreList = ['Strategy', 'Family', 'Adventure', 'Abstract', 'Party', 'Abstract Strategy','Card Game', 'Dice', 'Economic', 'Fantasy','Fighting','Electronic', 'Environmental', 'Horror', 'Humor', 'Mafia', 'Age of Reason', 'City Building'];
</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>

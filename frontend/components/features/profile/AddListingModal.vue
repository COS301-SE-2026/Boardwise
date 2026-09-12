<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6" style="background: var(--color-surface) !important; overflow-y: auto;">
      <v-form ref="formRef" v-model="formValid" class="d-flex flex-column ga-5">
        <h2>Create Listing</h2>

        <v-alert
          v-if="submitError"
          type="error"
          variant="tonal"
          density="compact"
          closable
          @click:close="submitError = ''"
        >
          {{ submitError }}
        </v-alert>

        <v-text-field
          v-model="listingTitle"
          label="Listing Title"
          placeholder="Listing title"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <v-autocomplete
          v-model="gameTitle"
          label="Game Title"
          :items="games"
          :loading="gamesLoading"
          item-title="title"
          item-value="title"
          no-filter
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
          @update:search="onGameSearch"
        />
        <v-text-field
          v-model="version"
          label="Version"
          placeholder="e.g. Original"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <v-select
          v-model="selectedCondition"
          label="Condition"
          :items="conditions"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <v-select
          v-model="selectedItemType"
          label="Item Type"
          :items="itemTypes"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <div class="d-flex">
          <v-btn-toggle v-model="listingType" color="primary" variant="outlined" mandatory divided>
            <v-btn value="sell">Sell</v-btn>
            <v-btn value="rent">Rent</v-btn>
          </v-btn-toggle>
        </div>

        <v-text-field
          v-model="price"
          label="Amount"
          prefix="R"
          placeholder="e.g. 650"
          type="number"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[priceRule]"
        />

        <div class="RentalPeriod">
          <div v-if="listingType === 'rent'" class="d-flex flex-column ga-3">
            <v-date-input
              v-model="startDate"
              label="Start Date"
              variant="outlined"
              hide-details="auto"
              :rules="[startDateRule]"
            />
            <v-date-input
              v-model="endDate"
              label="End Date"
              variant="outlined"
              hide-details="auto"
              :rules="[endDateRule]"
            />
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
            hide-details="auto"
            :loading="useCurrLocation && loading"
            :readonly="useCurrLocation && loading"
            :rules="[requiredRule]"
          />
          <p v-if="useCurrLocation && locationError" class="text-error text-caption mt-1">
            Couldn't get your location, please enter it manually.
          </p>
        </div>

        <v-textarea
          v-model="description"
          label="Description"
          placeholder="description"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <div class="d-flex flex-column ga-1">
          <div class="d-flex align-center ga-3">
            <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Image</v-btn>
            <label for="image-upload" class="text-grey text-body-2">{{ fileName || '···' }}</label>
            <input
              id="image-upload"
              ref="file_input"
              type="file"
              accept="image/*"
              class="hidden-input"
              @change="handleFileChange"
            />
          </div>
          <p v-if="fileError" class="text-error text-caption">{{ fileError }}</p>
        </div>

        <div class="d-flex justify-end ga-3">
          <v-btn variant="outlined" color="primary" :disabled="isLoading" @click="closeModal">Cancel</v-btn>
          <v-btn color="primary" @click="handleConfirm" :loading="isLoading" :disabled="isLoading">Create Listing</v-btn>
        </div>
      </v-form>
    </BaseCard>
  </v-dialog>
</template>

<script setup>
import { useUserLocation } from '@/composables/useUserLocation';
import { useBoardGames } from '~/composables/useBoardGames'
import BaseCard from '~/components/ui/BaseCard.vue'

const { city, suburb, lat, long, error: locationError, loading, findUserLocation } = useUserLocation();
const {searchGames, games } = useBoardGames();

onMounted(() => {
  searchGames()
})



let gameSearchTimeout
const onGameSearch = (query) => {
  clearTimeout(gameSearchTimeout)
  gameSearchTimeout = setTimeout(() => searchGames(query), 300)
}

const isLoading = ref(false);

const open = defineModel({
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
const location = ref('');
const fileName = ref('');
const file_input = ref(null);
const file = ref(null);
const version = ref('');
const useCurrLocation = ref(false);

const selectedCondition = ref(null);
const selectedItemType = ref(null);

const startDate = ref(null);
const endDate = ref(null);

const formRef = ref(null);
const formValid = ref(true);
const submitError = ref('');
const fileError = ref('');

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

// validation rules 
const requiredRule = (v) =>
  (v !== null && v !== undefined && String(v).trim() !== '') || 'This field is required';


const priceRule = (v) => {
  if (v === null || v === undefined || v === '') return 'Enter an amount';
  const n = Number(v);
  if (Number.isNaN(n)) return 'Enter a valid amount';
  if (n < 0) return 'Amount cannot be negative';
  return true;
};

const startOfDay = (d) => {
  const date = new Date(d);
  date.setHours(0, 0, 0, 0);
  return date;
};

const startDateRule = (v) => {
  if (listingType.value !== 'rent') return true;
  if (!v) return 'Start date is required';
  return startOfDay(v) >= startOfDay(new Date()) || 'Start date cannot be in the past';
};

const endDateRule = (v) => {
  if (listingType.value !== 'rent') return true;
  if (!v) return 'End date is required';
  if (startOfDay(v) < startOfDay(new Date())) return 'End date cannot be in the past';
  if (startDate.value && startOfDay(v) < startOfDay(startDate.value)) {
    return 'End date must be after start date';
  }
  return true;
};

const MAX_FILE_SIZE = 100 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

const triggerUpload = () => file_input.value.click();

const handleFileChange = (e) => {
  fileError.value = '';
  const toUpload = e.target.files[0];
  if (!toUpload) return;

  if (!ALLOWED_IMAGE_TYPES.includes(toUpload.type)) {
    fileError.value = 'Please upload a JPEG, PNG, WEBP or GIF image.';
  } else if (toUpload.size > MAX_FILE_SIZE) {
    fileError.value = 'Image must be smaller than 5MB.';
  }

  if (fileError.value) {
    e.target.value = '';
    fileName.value = '';
    file.value = null;
    return;
  }

  fileName.value = toUpload.name;
  file.value = toUpload;
}

const closeModal = () => {
  isLoading.value = false;
  open.value = false;
  listingTitle.value = '';
  gameTitle.value = '';
  version.value = '';
  description.value = '';
  selectedCondition.value = '';
  selectedItemType.value = '';
  listingType.value = 'sell';
  price.value = '';
  negotiable.value = false;
  location.value = '';
  fileName.value = '';
  file.value = null;
  useCurrLocation.value = false;
  submitError.value = '';
  fileError.value = '';
  formRef.value?.resetValidation();
}

function getRentalPeriod() {
  const fmt = (d) => {
    const date = new Date(d);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
  return [fmt(startDate.value), fmt(endDate.value)];
}

function getValidItemType() {
  return selectedItemType.value.toLowerCase();
}

function getValidCondition() {
  return selectedCondition.value.toLowerCase();
}

const handleConfirm = async () => {
  submitError.value = '';

  const { valid } = await formRef.value.validate();
  if (!valid) return;

  if (fileError.value) return;

  isLoading.value = true;

  try {
    const payload = {
      listingTitle: listingTitle.value,
      gameTitle: gameTitle.value,
      listingType: listingType.value === 'rent' ? 'rental' : 'sale',
      price: Number(price.value),
      itemType: getValidItemType(),
      condition: getValidCondition(),
      version: version.value,
      location: location.value,
      description: description.value,
      isNegotiable: negotiable.value,
      rentalPeriod: listingType.value === 'rent' ? getRentalPeriod() : null,
    };

    emit('confirm', payload, file.value, (errorMessage) => {
      isLoading.value = false;
      if (errorMessage) {
        submitError.value = errorMessage;
      } else {
        closeModal();
      }
    });
  } catch (err) {
    isLoading.value = false;
    submitError.value = err?.message || 'Something went wrong building your listing. Please try again.';
  }
}

const conditions = ['New', 'Like New', 'Good', 'Fair'];

const itemTypes = ["Merch", "Full Boardgame", "Partial Boardgame", "Pieces"];

</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>
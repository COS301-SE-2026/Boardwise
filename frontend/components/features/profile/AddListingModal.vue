<template>
    <BaseModal v-model="open" title="Create Listing" max-width="700" :loading="isLoading">
      <v-form ref="formRef" v-model="formValid" class="listing-form" @submmit.prevent="handleConfirm">
        <v-alert
          v-if="submitError"
          type="error"
          variant="tonal"
          density="compact"
          closable
          class="listing-form__alert"
          @click:close="submitError = ''"
        >
          {{ submitError }}
        </v-alert>

        <v-text-field
          v-model="listingTitle"
          label="Listing Title"
          placeholder="e.g Catan - Complete Set"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
          :rules="[requiredRule, listingTitleRule]"
          :disabled="isLoading"
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
          density="comfortable"
          hide-details="auto"
          :rules="[requiredRule]"
          :disabled="isLoading"
          @update:search="onGameSearch"
        />

        <v-text-field
          v-model="version"
          label="Version"
          placeholder="e.g. Original"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
          :rules="[requiredRule, versionRule]"
          :disabled="isLoading"
        />

        <v-select
          v-model="selectedCondition"
          label="Condition"
          :items="conditions"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
          :rules="[requiredRule]"
          :disabled="isLoading"
        />

        <v-select
          v-model="selectedItemType"
          label="Item Type"
          :items="itemTypes"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
          :disabled="isLoading"
        />

        <div class="listing-form__section">
          <p class="listing-form__label">Listing Type</p>
          <v-btn-toggle class="listing-type-toggle" v-model="listingType" mandatory divided :disabled="isLoading">
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
          min="0"
          step="1"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
          :rules="[priceRule]"
          :disabled="isLoading"
          @keydown="blockNegativeKeys"
          @paste="blockNegativePaste"
        />

        <div class="listing-form__section">
          <template v-if="listingType === 'rent'">
            <p class="listing-form__label">
              Rental Period
            </p>
            <div class="listing-form__date-grid">

              <v-date-input
                v-model="startDate"
                label="Start Date"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                :min="today"
                :disabled="isLoading"
                @keydown="blockManualDateEntry"
                :rules="[startDateRule]"
              />

              <v-date-input
                v-model="endDate"
                label="End Date"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                :min="today"
                :disabled="isLoading"
                @keydown="blockManualDateEntry"
                :rules="[endDateRule]"
              />
            </div>
          </template>

          <v-checkbox 
            v-else 
            v-model="negotiable" 
            label="Open to negotiation" 
            color="primary" 
            density="compact" 
            hide-details 
            :disabled="isLoading"
          />
        </div>

        <div class="listing-form__section">
          <v-switch 
            v-model="useCurrLocation"
            label="Use current location" 
            color="primary"
            density="compact"
            hide-details
            :disabled="isLoading"
          />

          <v-text-field
            v-model="location"
            label="Location"
            placeholder="e.g. Pretoria"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            :loading="useCurrLocation && loading"
            :readonly="useCurrLocation && loading"
            :rules="[requiredRule]"
            :disabled="isLoading"
          />

          <p 
            v-if="useCurrLocation && locationError" 
            class="listing-form__field-error"
          >
              Couldn't get your location, please enter it manually.
          </p>
        </div>

        <BaseTextArea
          v-model="description"
          label="Description"
          placeholder="Tell buyers about the game's condition, controls and anything else they should know."
          variant="outlined"
          density="comfortable"
          hide-details="auto"
          :rules="[requiredRule, descriptionRule]"
          :disabled="isLoading"
        />

        <div class="listing-form__section">
          <p class="listing-form__label">
            Listing Image
          </p>

          <div class="listing-form__upload">
            <BaseButton variant="secondary" color="primary" type="button" @click="triggerUpload" :disabled="isLoading">Upload Image</BaseButton>

            <span class="listing-form__file-name" :class="{ 'listing-form__file-name--empty' : !fileName }">
              {{ fileName || 'No image selected' }}
            </span>

            <label for="image-upload" class="sr-only">Upload listing image</label>

            <input
              id="image-upload"
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden-input"
              :disabled="isLoading"
              @change="handleFileChange"
            />
          </div>

          <p class="listing-form__hint">
            JPEG, PNG, WEBP or GIF · Maximum 50 MB
          </p>

          <p v-if="fileError" class="listing-form__field-error">{{ fileError }}</p>
        </div>

        <div class="listing-form__actions">
          <BaseButton variant="secondary" type="button" :disabled="isLoading" @click="closeModal">Cancel</BaseButton>
          <BaseButton type="submit" @click="handleConfirm" :loading="isLoading" :disabled="isLoading">Create Listing</BaseButton>
        </div>
      </v-form>
    </BaseModal>
</template>

<script setup>
import { useUserLocation } from '@/composables/useUserLocation'
import { useBoardGames } from '~/composables/useBoardGames'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const { city, suburb, error: locationError, loading, findUserLocation } = useUserLocation();
const { searchGames, games, gamesLoading } = useBoardGames();

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
  console.log("location: ", location.value);

});

const requiredRule = (v) =>
  (v !== null && v !== undefined && String(v).trim() !== '') || 'This field is required';

const listingTitleRule = (v) => {
  const required = requiredRule(v);
  if (required !== true) return required;
  if (String(v).length < 3) return 'Title must be at least 3 characters';
  if (String(v).length > 100) return 'Title cannot exceed 100 characters';
  return true;
};

const versionRule = (v) => {
  const required = requiredRule(v);
  if (required !== true) return required;
  if (String(v).length > 50) return 'Version cannot exceed 50 characters';
  return true;
};

const priceRule = (v) => {
  if (v === null || v === undefined || v === '') return 'Enter an amount';
  const n = Number(v);
  if (Number.isNaN(n)) return 'Enter a valid amount';
  if (!Number.isFinite(n)) return 'Enter a valid amount';
  if (n <= 0) return 'Amount must be greater than 0';
  return true;
};

const startOfDay = (d) => {
  const date = new Date(d);
  date.setHours(0, 0, 0, 0);
  return date;
};

// blocks typed keystrokes in the date fields while still letting the calendar picker open/close and tabbing work
const blockManualDateEntry = (e) => {
  const allowed = ['Tab', 'Shift', 'Escape', 'Enter'];
  if (!allowed.includes(e.key)) {
    e.preventDefault();
  }
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
  if (startDate.value) {
    const start = startOfDay(startDate.value);
    const end = startOfDay(v);
    if (end < start) return 'End date must be after start date';
    }
  return true;
};

 

const descriptionRule = (v) => {
  const required = requiredRule(v);
  if (required !== true) return required;
  return true;
};

const MAX_FILE_SIZE = 50 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif']);

const triggerUpload = () => file_input.value.click();

const handleFileChange = (e) => {
  fileError.value = '';
  const toUpload = e.target.files[0];
  if (!toUpload) return;

  if (!ALLOWED_IMAGE_TYPES.includes(toUpload.type)) {
    fileError.value = 'Please upload a JPEG, PNG, WEBP or GIF image.';
  } else if (toUpload.size > MAX_FILE_SIZE) {
    fileError.value = `Image must be smaller than ${MAX_FILE_SIZE / 1024 / 1024}MB.`;
  }

  if (fileError.value) {
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
  selectedCondition.value = null;
  selectedItemType.value = null;
  listingType.value = 'sell';
  price.value = '';
  negotiable.value = false;
  location.value = '';
  fileName.value = '';
  file.value = null;
  startDate.value = null;
  endDate.value = null;
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

const handleConfirm = async () => {
  submitError.value = '';

  const { valid } = await formRef.value.validate();
  if (!valid) return;

  if (fileError.value) return;

  if (!file.value) {
    fileError.value = 'Please upload an image';
    return;
  }

  isLoading.value = true;

  try {
    const payload = {
      listingTitle: listingTitle.value,
      gameTitle: gameTitle.value,
      listingType: listingType.value === 'rent' ? 'rental' : 'sale',
      price: Number(price.value),
      itemType: selectedItemType.value.toLowerCase(),
      condition: selectedCondition.value.toLowerCase(),
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

const itemTypes = ['Merch', 'Full Boardgame', 'Partial Boardgame', 'Pieces'];

</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>
<template>
  <BaseModal v-model="open">

    <div class="content">

      <h2>Create Listing</h2>

      <div class="input-group">
        <label>Listing Title</label>
        <BaseInput v-model="title" placeholder="Game title" />
      </div>

      <div class="input-group">
        <label>Type</label>
        <div class="toggle-row">
          <button :class="['toggle-btn', { active: type === 'sell' }]" @click="type = 'sell'">Sell</button>
          <button :class="['toggle-btn', { active: type === 'rent' }]" @click="type = 'rent'">Rent</button>
        </div>
      </div>

      <div v-if="type" class="input-group">
        <label>Amount (R)</label>
        <BaseInput v-model="price" placeholder="e.g. 650" type="number" />
      </div>

      <div v-if="type === 'rent'" class="input-group">
        <label>Rental Period</label>
        <select v-model="rentalPeriod" class="select">
          <option value="" disabled>Select period</option>
          <option value="1d">1 day</option>
          <option value="3d">3 days</option>
          <option value="1w">1 week</option>
          <option value="2w">2 weeks</option>
          <option value="1m">1 month</option>
        </select>
      </div>

      <div v-if="type === 'sell'" class="input-group checkbox-row">
        <input id="negotiate" v-model="negotiable" type="checkbox" />
        <label for="negotiate">Open to negotiation</label>
      </div>

      <div class="input-group">
        <label>Location</label>
        <BaseInput v-model="location" placeholder="e.g. Pretoria" />
      </div>

      <!-- <div class="input-group">
        <label>Image URL</label>
        <BaseInput v-model="image" placeholder="https://..." />
      </div> -->

      <div class="input-group">
        <label>Game Cover</label>
        <div class="upload-row">
          <BaseButton variant="secondary" @click="triggerUpload">
            Upload Image
          </BaseButton>
          <span class="filename">{{ fileName || '···' }}</span>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="hidden-input"
            @change="handleFileChange"
          />
        </div>
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleConfirm">Create Listing</BaseButton>
      </div>

    </div>

  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
const emit = defineEmits(['confirm'])

const title = ref('')
const type = ref('')
const rentalPeriod = ref('')
const negotiable = ref(false)
const price = ref('')
const location = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileChange = (e) =>{
  const toUpload = e.target.files[0];
  if (toUpload){
    fileName.value = toUpload.name;
    file.value = toUpload;
  }
}

const closeModal = () => {
  open.value = false
  title.value = ''
  type.value = ''
  price.value = ''
  rentalPeriod.value = ''
  negotiable.value = false
  location.value = ''
  fileName.value = ''
}

//TEMPORARY FIX:
const rentalDuration = ref('1w');

const getRentalPeriod = () =>{
  const start = new Date();
  const end = new Date();

  switch (rentalDuration.value) {
    case '1d':
    end.setDate(end.getDate() + 1); 
    break;
    case '3d':
    end.setDate(end.getDate() + 3); 
    break;
    case '1w':
    end.setDate(end.getDate() + 7); 
    break;
    case '2w':
    end.setDate(end.getDate() + 14); 
    break;
    case '1m': 
    end.setMonth(end.getMonth() + 1); 
    break;
  }

  return[
      start.toISOString().split('T')[0].toString(),
      end.toISOString().split('T')[0].toString()
    ]
  
}

const file = ref(null);



const handleConfirm = () => {
  console.log('clicked', title.value, type.value, price.value);
  if (!title.value || !type.value || price.value < 0 || !fileName.value) return
  emit('confirm', {
    gameTitle: title.value,
    listingType: (type.value === 'rent')?'rental':'sale',
    price: price.value,
    itemType: 'boardgame',
    description: "This is the board game",
    genres: ['strategy', 'negotiation'],
    rentalPeriod: type.value === 'rent'? getRentalPeriod(): null ,
    // negotiable: negotiable.value,
    // location: location.value,
  },file.value)
  closeModal()
}

</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 8px;
  box-sizing: border-box;
  width: 100%;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filename {
  font-size: 14px;
  color: #888;
}

.hidden-input {
  display: none;
}
</style>
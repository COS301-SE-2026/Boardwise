Your EditListingModal.vue script wasn't updated — it still has the old refs and is missing all the new fields. Replace the entire file:
vue<template>
  <BaseModal v-model="open">
    <div class="content">

      <h2>Edit Listing</h2>

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
          <option>1 day</option>
          <option>3 days</option>
          <option>1 week</option>
          <option>2 weeks</option>
          <option>1 month</option>
        </select>
      </div>

      <div v-if="type === 'sell'" class="input-group checkbox-row">
        <input id="negotiate-edit" v-model="negotiable" type="checkbox" />
        <label for="negotiate-edit">Open to negotiation</label>
      </div>

      <div class="input-group">
        <label>Location</label>
        <BaseInput v-model="location" placeholder="e.g. Pretoria" />
      </div>

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
        <BaseButton variant="secondary" @click="open = false">Cancel</BaseButton>
        <BaseButton @click="handleSave">Save Changes</BaseButton>
      </div>

    </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

const open = defineModel()
const props = defineProps({ listing: Object })
const emit = defineEmits(['save'])

const title = ref(props.listing?.title ?? '')
const type = ref(props.listing?.type ?? '')
const price = ref(props.listing?.price ?? '')
const rentalPeriod = ref(props.listing?.rentalPeriod ?? '')
const negotiable = ref(props.listing?.negotiable ?? false)
const location = ref(props.listing?.location ?? '')
const fileName = ref(props.listing?.image ?? '')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const handleSave = () => {
  emit('save', {
    title: title.value,
    type: type.value,
    price: price.value,
    rentalPeriod: rentalPeriod.value,
    negotiable: negotiable.value,
    location: location.value,
    image: fileName.value
  })
  open.value = false
}
</script>

<style scoped>
.content { display: flex; flex-direction: column; gap: 20px; }
h2 { margin: 0; font-size: 22px; }
.input-group { display: flex; flex-direction: column; gap: 8px; }

label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.toggle-row { display: flex; gap: 12px; }

.toggle-btn {
  flex: 1;
  padding: 10px;
  border: 2px solid #ddd;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  transition: all 0.2s;
}

.toggle-btn.active {
  border-color: #6C3BFF;
  background: #f3eeff;
  color: #6C3BFF;
}

.select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.checkbox-row {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.checkbox-row input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #6C3BFF;
}

.checkbox-row label { font-weight: 400; }
.upload-row { display: flex; align-items: center; gap: 12px; }
.filename { font-size: 14px; color: #888; }
.hidden-input { display: none; }

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
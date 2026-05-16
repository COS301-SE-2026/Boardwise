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
          <option>1 day</option>
          <option>3 days</option>
          <option>1 week</option>
          <option>2 weeks</option>
          <option>1 month</option>
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

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if(file) fileName.value = file.name
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

const handleConfirm = () => {
  if (!title.value || !type.value) return
  emit('confirm', {
    title: title.value,
    type: type.value,
    price: price.value,
    rentalPeriod: rentalPeriod.value,
    negotiable: negotiable.value,
    location: location.value,
    image: fileName.value
  })
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
<template>
  <BaseModal v-model="open">
    <div class="content">

      <h2>Add a Game</h2>

      <div class="form">
        <div class="input-group">
          <label>Game Name</label>
          <BaseInput
            v-model="title"
            placeholder="Please enter"
          />
        </div>

        <div class="input-group">
          <label>Game Genre / Category</label>
          <select v-model="category" class="select">
            <option value="" disabled>Select Option</option>
            <option>Strategy</option>
            <option>Family</option>
            <option>Abstract</option>
            <option>Party</option>
            <option>Cooperative</option>
            <option>Thematic</option>
            <option>War</option>
            <option>Other</option>
          </select>
        </div>

        <div class="input-group">
          <label>Game Cover</label>
          <div class="upload-row">
            <BaseButton variant="secondary" @click="triggerUpload">
              Upload Game Cover
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
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">
          Cancel
        </BaseButton>
        <BaseButton @click="handleConfirm">
          Add
        </BaseButton>
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
const category = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const closeModal = () => {
  open.value = false
  title.value = ''
  category.value = ''
  fileName.value = ''
}

const handleConfirm = () => {
  if (!title.value) return
  emit('confirm', {
    title: title.value,
    category: category.value,
    image: fileName.value
  })
  closeModal()
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

h2 {
  margin: 0;
  font-size: 24px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
  appearance: auto;
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

.actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
</style>
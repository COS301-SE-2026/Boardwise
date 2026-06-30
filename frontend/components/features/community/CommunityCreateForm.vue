<template>
  <BaseModal v-model="open">
    <div class="content">

      <h2>Create Community</h2>

      <div class="form">

        <div class="input-group">
          <label>Community Name</label>
          <BaseInput v-model="name" placeholder="e.g. Catan Lovers" />
        </div>

        <div class="input-group">
          <label>Description</label>
          <BaseTextArea v-model="description" placeholder="What is this community about?" :rows="3" />
        </div>

        <div class="input-group">
          <label>Category</label>
          <select v-model="category" class="select">
            <option value="" disabled>Select a category</option>
            <option>Strategy</option>
            <option>Family</option>
            <option>Party</option>
            <option>Cooperative</option>
            <option>General</option>
          </select>
        </div>

        <div class="input-group">
          <label>Type</label>
          <div class="toggle-row">
            <button :class="['toggle-btn', { active: type === 'Public' }]" @click="type = 'Public'">Public</button>
            <button :class="['toggle-btn', { active: type === 'Private' }]" @click="type = 'Private'">Private</button>
          </div>
        </div>

        <div class="input-group">
          <label>Community Image</label>
          <div class="upload-row">
            <BaseButton @click="triggerUpload">Upload Image</BaseButton>
            <span class="filename">{{ fileName || '···' }}</span>
            <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
          </div>
        </div>

      </div>

      <div class="actions">
        <BaseButton @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleCreate">Create</BaseButton>
      </div>

    </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
const emit = defineEmits(['confirm'])

const name = ref('')
const description = ref('')
const category = ref('')
const type = ref('Public')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const closeModal = () => {
  open.value = false
  name.value = ''
  description.value = ''
  category.value = ''
  type.value = 'Public'
  fileName.value = ''
}

const handleCreate = () => {
  if (!name.value.trim()) return
  emit('confirm', {
    id: Date.now(),
    name: name.value,
    description: description.value,
    category: category.value,
    type: type.value,
    image: fileName.value || '/images/castle.png',
    members: 1,
    members_list: []
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
  font-size: 22px;
  font-weight: 700;
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
}

.toggle-row {
  display: flex;
  gap: 12px;
}

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
  transition: all 0.15s;
}

.toggle-btn.active {
  border-color: #6C3BFF;
  background: #f3eeff;
  color: #6C3BFF;
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
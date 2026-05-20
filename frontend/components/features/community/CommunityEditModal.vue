<template>
  <BaseModal v-model="open">
    <div class="content">

      <h2>Edit Community</h2>

      <div class="input-group">
        <label>Community Name</label>
        <BaseInput v-model="name" placeholder="Community name" />
      </div>

      <div class="input-group">
        <label>Description</label>
        <BaseTextArea v-model="description" placeholder="What is this community about?" :rows="3" />
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
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
const props = defineProps({ community: Object })
const emit = defineEmits(['save'])

const name = ref(props.community?.name ?? '')
const description = ref(props.community?.description ?? '')
const type = ref(props.community?.type ?? 'Public')
const fileName = ref(props.community?.image ?? '')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const handleSave = () => {
  emit('save', {
    name: name.value,
    description: description.value,
    type: type.value,
    image: fileName.value
  })
  open.value = false
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

h2 {
  margin: 0;
  font-size: 22px;
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
  transition: all 0.2s;
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
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
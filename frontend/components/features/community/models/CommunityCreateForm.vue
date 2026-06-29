<template>
  <BaseModal v-model="open">
    <div class="content">
      <h2>Create Community</h2>

      <div class="form">
        <div class="input-group">
          <label for="create-name">Community Name</label>
          <BaseInput id="create-name" v-model="name" placeholder="e.g. Catan Lovers" />
        </div>

        <div class="input-group">
          <label for="create-description">Description</label>
          <BaseTextArea v-model="description" placeholder="What is this community about?" :rows="3" />
        </div>

        <div class="input-group">
          <label for="create-category">Category</label>
          <select id="create-category" v-model="category" class="select">
            <option value="" disabled>Select a category</option>
            <option>Strategy</option>
            <option>Family</option>
            <option>Party</option>
            <option>Cooperative</option>
            <option>General</option>
          </select>
        </div>

        <div class="input-group">
          <span class="label-text">Type</span>
          <div class="toggle-row">
            <button 
              :class="['toggle-btn', { active: type === 'Public' }]" 
              @click="type = 'Public'"
            >
              Public
            </button>
            <button 
              :class="['toggle-btn', { active: type === 'Private' }]" 
              @click="type = 'Private'"
            >
              Private
            </button>
          </div>
        </div>

        <div class="input-group">
          <label for="create-image">Community Image</label>
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
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleCreate">Create</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const open = ref(props.modelValue)
const name = ref('')
const description = ref('')
const category = ref('')
const type = ref('Public')
const fileName = ref('')
const fileInput = ref(null)

watch(() => props.modelValue, (val) => {
  open.value = val
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

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
    image: fileName.value || '/images/community-default.jpg',
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
  gap: var(--space-6);
}

.content h2 {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.input-group label {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.select {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--fs-body);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
}

.select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(109, 0, 55, 0.15);
}

.toggle-row {
  display: flex;
  gap: var(--space-3);
}

.toggle-btn {
  flex: 1;
  padding: var(--space-2) var(--space-4);
  border: 2px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  cursor: pointer;
  font-size: var(--fs-body);
  font-weight: var(--fw-medium);
  color: var(--color-text-muted);
  transition: all var(--transition-base);
}

.toggle-btn.active {
  border-color: var(--color-primary);
  background: rgba(109, 0, 55, 0.06);
  color: var(--color-primary);
}

.upload-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.filename {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.hidden-input {
  display: none;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
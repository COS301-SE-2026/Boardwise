<template>
  <BaseModal v-model="open">
    <div class="content">
      <h2>Edit Community</h2>

      <div class="input-group">
        <label for="edit-name">Community Name</label>
        <BaseInput 
          id="edit-name"
          v-model="name" 
          placeholder="Community name" 
        />
      </div>

      <div class="input-group">
        <label for="edit-description">Description</label>
        <BaseTextArea 
          id="edit-description"
          v-model="description" 
          placeholder="What is this community about?" 
          :rows="3" 
        />
      </div>

      <div class="input-group">
        <label>Type</label>
        <div class="toggle-row" role="group" aria-label="Community Type">
          <button 
            :class="['toggle-btn', { active: type === 'Public' }]" 
            @click="type = 'Public'"
            role="radio"
            :aria-checked="type === 'Public'"
          >
            Public
          </button>
          <button 
            :class="['toggle-btn', { active: type === 'Private' }]" 
            @click="type = 'Private'"
            role="radio"
            :aria-checked="type === 'Private'"
          >
            Private
          </button>
        </div>
      </div>

      <div class="input-group">
        <label for="edit-image">Community Image</label>
        <div class="upload-row">
          <BaseButton variant="secondary" @click="triggerUpload">
            Upload Image
          </BaseButton>
          <span class="filename">{{ fileName || '···' }}</span>
          <input
            id="edit-image"
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
import { ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  modelValue: Boolean,
  community: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'save'])

const open = ref(props.modelValue)
const name = ref(props.community.name || '')
const description = ref(props.community.description || '')
const type = ref(props.community.type || 'Public')
const fileName = ref(props.community.image || '')
const fileInput = ref(null)

watch(() => props.modelValue, (val) => {
  open.value = val
  if (val) {
    name.value = props.community.name || ''
    description.value = props.community.description || ''
    type.value = props.community.type || 'Public'
    fileName.value = props.community.image || ''
  }
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const handleSave = () => {
  if (!name.value.trim() || !description.value.trim()) return
  emit('save', {
    name: name.value.trim(),
    description: description.value.trim(),
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
  gap: var(--space-5);
}

.content h2 {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
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

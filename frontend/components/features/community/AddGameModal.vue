<template>
  <BaseModal v-model="open">
    <div class="content">
      <h2>Add Game to Collection</h2>

      <div class="form">
        <div class="input-group">
          <label for="game-name">Game Name</label>
          <BaseInput id="game-name" v-model="name" placeholder="e.g. Catan" />
        </div>

        <div class="input-group">
          <label for="game-image">Image URL</label>
          <BaseInput id="game-image" v-model="image" placeholder="https://example.com/image.jpg" />
        </div>

        <div class="input-group">
          <label for="game-category">Category</label>
          <select id="game-category" v-model="category" class="select">
            <option value="" disabled>Select a category</option>
            <option>Strategy</option>
            <option>Family</option>
            <option>Party</option>
            <option>Cooperative</option>
            <option>General</option>
          </select>
        </div>

        <div class="input-group">
          <label for="game-tags">Tags (comma separated)</label>
          <BaseInput id="game-tags" v-model="tagsInput" placeholder="Strategy, Engine Building" />
        </div>

        <div class="input-group">
          <label>
            <input type="checkbox" v-model="isEssential" />
            Mark as Essential
          </label>
        </div>
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleAdd">Add Game</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const open = ref(props.modelValue)
const name = ref('')
const image = ref('')
const category = ref('')
const tagsInput = ref('')
const isEssential = ref(false)

watch(() => props.modelValue, (val) => {
  open.value = val
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

const tags = computed(() => {
  return tagsInput.value.split(',').map(t => t.trim()).filter(Boolean)
})

const closeModal = () => {
  open.value = false
  name.value = ''
  image.value = ''
  category.value = ''
  tagsInput.value = ''
  isEssential.value = false
}

const handleAdd = () => {
  if (!name.value.trim()) return
  emit('confirm', {
    id: Date.now(),
    name: name.value.trim(),
    title: name.value.trim(),
    image: image.value.trim() || '/images/game-default.jpg',
    category: category.value || 'General',
    tags: tags.value.length ? tags.value : [category.value || 'General'],
    isEssential: isEssential.value
  })
  closeModal()
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

.input-group label input[type="checkbox"] {
  margin-right: var(--space-2);
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

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
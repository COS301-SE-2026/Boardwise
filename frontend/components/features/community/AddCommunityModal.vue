<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-6">
      <h2>Add Community</h2>

      <BaseInput 
            v-model="form.name" 
            label="Community Name" 
          />
        
       <BaseTextArea 
            v-model="form.description" 
            placeholder="What is this community about?" 
            :rows="3" 
          />
        </div>

        <v-select
        v-model="form.category"
        :items="categories"
        label="Category"
        variant="outlined"
        rounded="lg"
      />

         <v-btn-toggle
        v-model="form.type"
        mandatory
        divided
      >
        <v-btn value="Public">
          Public
        </v-btn>

        <v-btn value="Private">
          Private
        </v-btn>
      </v-btn-toggle>

        <div class="d-flex align-center ga-4">
        <label for="community-image-upload">
        <BaseButton >
          Upload Image
        </BaseButton>
      </label>

        <span class="text-body-2 text-medium-emphasis">
          {{ fileName || 'No file selected' }}
        </span>
        
        <label
            for="community-image-upload"
            class="d-none"
          >
            Community image
          </label>

        <input 
              id="community-image-upload"
              ref="fileInput" 
              type="file" 
              accept="image/*" 
              class="d-none" 
              @change="handleFileChange" 
            />
          </div>

        <div class="d-flex justify-end ga-3">
        <BaseButton
          variant="secondary"
          @click="closeModal"
        >
          Cancel
        </BaseButton>

        <BaseButton @click="handleAdd">
          Add Community
        </BaseButton>
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
  type: Boolean,
  default: false
})

const emit = defineEmits(['confirm'])

const categories = [
  'Strategy',
  'Family',
  'Party',
  'Cooperative',
  'General'
]

const form = reactive({
  name: '',
  description: '',
  category: '',
  type: 'Public'
})

const fileName = ref('')
const fileInput = ref(null)

watch(() => props.modelValue, (val) => {
  open.value = val
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

const handleFileChange = (event) => {
  const file = event.target.files?.[0]

  if (file) {
    fileName.value = file.name
  }
}

const closeModal = () => {
  open.value = false
  name.value = ''
  description.value = ''
  category.value = ''
  visibility.value = 'Public'
  fileName.value = ''
}

const handleAdd = () => {
  if (!name.value.trim() || !description.value.trim()) return
  emit('confirm', {
    id: Date.now(),
    name: name.value.trim(),
    description: description.value.trim(),
    category: category.value || 'General',
    type: visibility.value,
    image: fileName.value || '/images/community-default.jpg',
    members: 1,
    members_list: [{ 
      id: Date.now(), 
      name: 'You', 
      role: 'Admin', 
      avatar: '/images/avatar.jpg' }]
  })
  closeModal()
}
</script>

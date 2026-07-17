<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-6">

      <h2>Edit Community</h2>

        <BaseInput 
          v-model="name" 
          placeholder="Community name" 
          />
      
        <BaseTextArea 
            v-model="description" 
            placeholder="What is this community about?" 
            :rows="3" 
            />

      <v-btn-toggle
        v-model="form.type"
        mandatory
        divided  
      >
        <BaseButton>
          Public
        </BaseButton>

        <BaseButton>
          Private
        </BaseButton>
    </v-btn-toggle>

      <div class="d-flex align-center ga-4">
          <BaseButton @click="fileInput?.click()">
            Upload Image
          </BaseButton>

          <span class="filename">{{ fileName || '···' }}</span>

          <BaseInput
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

        <BaseButton 
          @click="handleSave">
          Save Changes
        </BaseButton>
      </div>

    </div>
  </BaseModal>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel({
  type: Boolean,
  default: false
})

const props = defineProps({ 
  community: {
    type: Object,
    required: true
  } 
})

const emit = defineEmits(['save'])

const form = reactive({
  name: '',
  description: '',
  type: 'Public'
})

const fileName = ref('')
const fileInput = ref(null)

watch(
  () => props.community,
  (community) => {
    if (!community) return
    
    form.name = community.name
    form.description = community.description
    form.type = community.type

    fileName.value = community.image
  },
  { immediate: true }
)

const handleFileChange = (event) => {

  const file = event.target.files[0]

  if (file) fileName.value = file.name
}

const closeModal = () => {
  open.value = false
}

const handleSave = () => {
  emit('save', {
    ...props.community,
    ...form,
    image: fileName.value
  })

  closeModal()
}
</script>

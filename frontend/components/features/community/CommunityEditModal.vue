<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-6">

      <h2>Edit Community</h2>

        <BaseInput 
          v-model="form.name" 
          placeholder="Community name" 
          />
      
        <BaseTextArea 
            v-model="form.description" 
            placeholder="What is this community about?" 
            :rows="3" 
            />

      <v-btn-toggle
        v-model="form.visibility"
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
          <BaseButton @click="fileInput?.click()">
            <v-icon start>mdi-upload</v-icon>
            Upload Image
          </BaseButton>

          <span class="filename">{{ fileName || '···' }}</span>

          <label for="community-image-upload" class="sr-only">Upload Image</label>
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
import { useSnackBar } from '~/composables/useSnackbar'
import { useCommunity } from '~/composables/useCommunity'

const { show } = useSnackBar()
const { editCommunity, error } = useCommunity()

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
const file = ref(null)

watch(
  () => props.community,
  (community) => {
    if (!community) return
    
    form.name = community.name
    form.description = community.description
    form.visibility = community.visibility

    fileName.value = community.image
  },
  { immediate: true }
)

const handleFileChange = (event) => {
  const chosenfile = event.target.files?.[0]

  if(chosenfile) {
    fileName.value = chosenfile.name
    file.value = chosenfile
  }

}

const closeModal = () => {
  open.value = false
}

const handleSave = async () => {
  try{
    const response = await editCommunity(
      props.community.id,
      file.value,
      {
        name: form.name,
        description: form.description,
        visibility: form.visibility
      }
    )
    emit('save', response.data)
  }
  catch(err){
    console.error("Failed to edit community details", err)
    show(error.value, 'error')
  }
  finally{
    closeModal()
  }
}
</script>

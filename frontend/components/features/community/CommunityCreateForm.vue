<template>
  <BaseModal v-model="open" >
    <div class="d-flex flex-column ga-6">

      <h2>Create Community</h2>

        <BaseInput
          v-model="form.name"
          label="Community Name"
        />

        <BaseTextArea
          v-model="form.description"
          placeholder="What is this comunity about ?"
          :rows="3"
        />

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


        <div class=" d-flex align-center ga-4">
          <BaseButton @click="fileInput?.click()" >
          Upload image
        </BaseButton>

        <span class=" text-body-2 text-medium-emphasis">
          {{ fileName || 'No file selected' }}
        </span>

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

        <BaseButton  @click="handleCreate"   >
          Create
      </BaseButton>
      </div>

    </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { reactive, ref } from 'vue'

const open = defineModel({
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

const fileInput = ref(null)
const fileName = ref('')

const handleFileChange = (event) => {
  const file = event.target.files?.[0]

  if(file) {
    fileName.value = file.name
  }

}

const closeModal  = () => {
  open.value = false

  form.name = ''
  form.description = ''
  form.category = ''
  form.type = 'Public'
  fileName.value = ''
}

const handleCreate = () => {
  if(!form.name.trim()) return

  emit('confirm', {
    id: Date.now(),
    ...form,
    image: fileName.value || '/images/castle.png',
    members: 1,
    memebers_list: []
  } )

 
  closeModal()
}
</script>

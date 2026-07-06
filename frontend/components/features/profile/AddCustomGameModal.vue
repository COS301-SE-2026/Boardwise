<template>
    <BaseModal v-model="open">
        <div class="d-flex align-center justify-space-between mb-4">
            <div>
                <h2>Add a custom game</h2>
                <p class="text-body-2 mb-0" style="color: var(--color-text-muted)">
                    This game isn't in our library yet
                </p>
            </div>

            <v-btn icon variant="text" @click="closeModal">
                <v-icon>mdi-close</v-icon>
            </v-btn>
        </div>

        <div class="d-flex flex-column ga-4 mb-6">
            <BaseInput
                v-model="title"
                label="Game Name"
                placeholder="e.g. Wingspan"
                variant="outlined"
                density="compact"
                hide-details
            />

            <v-select
                v-model="genre"
                label="Game Genre "
                :items="['Strategy','Family','Abstract','Party','Cooperative','Thematic','War','Other']"
                variant="outlined"
                density="compact"
                hide-details
            />

            <div class="d-flex align-center ga-3">
                <BaseButton variant="secondary" @click="triggerUpload">
                    <v-icon start>mdi-upload</v-icon>
                    Upload Cover
                </BaseButton>

                <span style="font-size: var(--fs-small); color: var(--color-text-muted)">
                    {{ fileName || 'No file chosen' }}
                </span>

                <label for="cover-upload" class="sr-only">Upload Game Cover</label>
                <input
                    id="cover-upload"
                    ref="fileInput"
                    type="file"
                    accept="image/*"
                    style="display: none;"
                    @change="handleFileChange"
                />
            </div>
        </div>
    </BaseModal>
</template>

<script setup>
import { ref } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
const emit = defineEmits(['confirm', 'back'])

const title     = ref('')
const genre     = ref('')
const fileName  = ref('')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()
const handleFileChange = (e) => {
    const file = e.target.files[0]
    if (file) fileName.value = file.name
}

const closeModal = () => {
    open.value = false
    title.value = ''
    genre.value = ''
    fileName.value = ''
}

const handleConfirm = () => {
    if(!title.value) return 
    emit('confirm', { title: title.value, genre: genre.value, image: fileName.value, custom: true })
    closeModal()
}

</script>
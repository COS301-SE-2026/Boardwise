<template>
    <BaseModal v-model="open" :max-width="600">
        <div class="d-flex align-center justify-space-between mb-5">
            <h2>Create event</h2>
            <v-btn icon variant="text" @click="open = false">
                <v-icon>mdi-close</v-icon>
            </v-btn>
        </div>

        <div class="d-flex flex-column ga-4 mb-6">
            <BaseInput
                v-model="form.name"
                label="Event name"
                placeholder="e.g. Catan Night"
                variant="outlined"
                density="compact"
                hide-details
            />

            <BaseInput
                v-model="form.description"
                label="Description"
                placeholder="What's the event about?"
                variant="outlined"
                density="compact"
                hide-details
            />

            <BaseInput
                v-model="form.date"
                label="Date"
                type="date"
                variant="outlined"
                density="compact"
                hide-details
            />

            <div class="d-flex ga-3">
                <BaseInput
                    v-model="form.description"
                    label="Description"
                    placeholder="What's the event about?"
                    variant="outlined"
                    density="compact"
                    hide-details
                />

                <BaseInput
                    v-model="form.startTime"
                    label="Start time"
                    type="time"
                    variant="outlined"
                    density="compact"
                    hide-details
                />

                <BaseInput
                    v-model="form.endTime"
                    label="End time"
                    type="time"
                    variant="outlined"
                    density="compact"
                    hide-details
                />
            </div>

            <BaseInput
                v-model="form.location"
                label="Location"
                placeholder="e.g 123 Main St, Pretoria"
                variant="outlined"
                density="compact"
                hide-details
            />

            <v-select 
                v-model="form.visibility"
                label="Visibility"
                :items="['PUBLIC', 'PRIVATE']"
                variant="outlined"
                density="compact"
                hide-details
            />

            <div class="d-flex align center ga-3">
                <BaseButton variant="secondary" @click="triggerUpload">
                    <v-icon start>mdi-image</v-icon>
                    Upload cover image
                </BaseButton>

                <span class="text-body-2 text-medium-emphasis">
                    {{ fileName || 'No file chosen' }}
                </span>

                <input
                    ref="fileInput"
                    type="file"
                    accept="image/*"
                    style="display: none;"
                    @change="handleFileChange"
                />
            </div>
        </div>

        <div class="d-flex justify-space-between ga-3">
            <BaseButton variant="secondary" @click="open = false">
                Cancel
            </BaseButton>

            <BaseButton :disabled="!isValid" :loading="isSubmitting" @click="handleSubmit">
                <v-icon start>mdi-calendar-plus</v-icon>
                Create Event
            </BaseButton>
        </div>
    </BaseModal>
</template>

<script setup>
import { ref, computed } from 'vue'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

const open = defineModel()
const emit = defineEmits(['created'])

const fileInput = ref(null)
const fileName = ref('')
const imageFile = ref(null)
const isSubmitting = ref(false)

const form = ref({
    name:        '',
    description: '',
    date:        '',
    startTime:   '',
    endTime:     '',
    location:    '',
    visibility:  'PUBLIC',
    games:       []
})

const isValid = computed(() =>
    form.value.name &&
    form.value.date &&
    form.value.startTime &&
    form.value.endTime &&
    form.value.location
)

const triggerUpload = () => fileInput.value.click()

const handleFileChange = (e) => {
    const file = e.target.files[0]
    if (file) {
        fileName.value = file.name
        imageFile.value = file
    }
}

const handleSubmit = async () => {
    if(!isValid.value) return
    isSubmitting.value = true

    try {
        emit('created', {
            eventInfo: { ...form.value},
            image: imageFile.value
        })

        open.value = false
    } finally {
        isSubmitting.value = false
    }
}

</script>
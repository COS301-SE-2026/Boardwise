<template>
    <BaseModal v-model="open" :max-width="600">
        <div class="d-flex align-center justify-space-between mb-5">
            <h2> Create Event</h2>
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

            <BaseTextArea
                v-model="form.description"
                placeholder="What's the event about?"
                :rows="4"
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

            <v-autocomplete
                v-model="form.games"
                :items="games"
                :loading="gamesLoading"
                item-title="title"
                item-value="id"
                label="Games"
                placeholder="Search for games to add"
                variant="outlined"
                density="compact"
                multiple
                chips
                closable-chips
                hide-details
                @update:search="onGameSearch"
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
import { ref, computed, watch,onMounted } from 'vue'

import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'

import { useBoardGames } from '~/composables/useBoardGames'
import { useSnackBar } from '~/composables/useSnackbar'

const { show } = useSnackBar(3)

const { games, isLoading: gamesLoading, searchGames } = useBoardGames()
onMounted(() => searchGames())


const open = defineModel()

const props = defineProps({
    onSubmit: {
        type: Function,
        required: true
    }
});

const emptyForm = ()=>(
    {
        name: '',
        description: '',
        date: '',
        startTime: '',
        endTime: '',
        location: '',
        visibility: 'PUBLIC',
        games:[]
    }
)



const emit = defineEmits(['created'])

const fileInput = ref(null)
const fileName = ref('')
const imageFile = ref(null)
const isSubmitting = ref(false)

const form = ref(emptyForm());

let searchTimeout
const onGameSearch = (query) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => searchGames(query), 400);
}
watch(open, (isOpen)=>{
    if(!isOpen){
        return;
    }

    form.value = emptyForm();
    fileName.value = '';
    imageFile.value = null;
})


const isValid = computed(() =>
    form.value.name &&
    form.value.date &&
    form.value.startTime &&
    form.value.endTime &&
    form.value.location &&
    form.value.games.length > 0

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
         const payload = {
            name: form.value.name,
            description: form.value.description,
            date: form.value.date,                    
            startTime: `${form.value.startTime}:00`,  
            endTime: `${form.value.endTime}:00`,
            location: form.value.location,
            visibility: form.value.visibility,
            games: form.value.games                
        }

        const createdEvent = await props.onSubmit({
            eventInfo:payload,
            image:imageFile.value
        });

        emit('created', createdEvent);
        open.value = false
    }
    catch(err){
        show(err?.data?.message || 'Failed to create event. Please try again.', 'error');

    }
    finally {
        isSubmitting.value = false
    }
}

</script>
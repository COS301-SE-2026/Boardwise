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
                v-model="genres"
                label="Game Genre "
                :items="genreOptions"
                variant="outlined"
                density="compact"
                hide-details
                multiple
                chips
            />

            <BaseInput
                v-model="description"
                label="Description"
                placeholder="Short description of the game"
                variant="outlined"
                density="compact"
                hide-details
            />

            <div class="d-flex ga-3">
                <BaseInput
                    v-model.number="minPlayers"
                    label="Min Players"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                />
                <BaseInput
                    v-model.number="maxPlayers"
                    label="Max Players"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                />                
            </div>

            <div class="d-flex ga-3">
                <BaseInput
                    v-model.number="minAge"
                    label="Minimum Age"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                />
                <BaseInput
                    v-model.number="duration"
                    label="Duration (minutes)"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                />
            </div>

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
import { ref,onMounted } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useProfile } from  '@/composables/useProfile'
import { userService } from '~/services/userService'

const { addGame, isLoading, error } = useProfile();

const open = defineModel({ type: Boolean, default: false })

const emit = defineEmits(['confirm', 'back'])

const title = ref('');
const description = ref('');
const minPlayers = ref(null);
const maxPlayers = ref(null);
const genres = ref([])
const minAge = ref(null);
const duration = ref(null);

const fileName  = ref('');
const fileInput = ref(null);
const file = ref(null);


const genreOptions = ref([]);


onMounted(async () =>{
    try{
        const res = await userService.getGenres();
        genreOptions.value = res.genres;
    }
    catch(err){
        console.error('failed to load genres: ', err);
    }
})


const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
    const chosenFile = e.target.files[0];
    if (chosenFile){
        file.value = chosenFile;
        fileName.value = chosenFile.name;
    }
}

const closeModal = () => {
    open.value = false;
    title.value = '';
    description.value = '';
    minPlayers.value = null;
    maxPlayers.value = null;
    minAge.value = null;
    duration.value = null;
    genres.value = [];
    fileName.value = '';
    file.value = null;
}

const handleConfirm = async () => {
    if (!title.value || !file.value || !description.value || !minPlayers.value || !maxPlayers.value   || !minAge.value || !duration.value) return

    if (minPlayers.value >= maxPlayers.value) {
        console.error('minPlayers must be less than maxPlayers')
        return
    }
    const gameData = {
        title: title.value,
        description: description.value,
        minPlayers: minPlayers.value,
        maxPlayers: maxPlayers.value,
        minAge: minAge.value,
        duration: duration.value,
        genres: genres.value
    }

    try{
        const res = await addGame(gameData,file.value);
        emit('confirm', res);
        closeModal();
    }
    catch(err){
        console.error("error while adding game: ", err);
    }
}

</script>
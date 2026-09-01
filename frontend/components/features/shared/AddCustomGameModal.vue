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
                v-bind="inputProps"
            />
            <v-autocomplete
                v-model="genres"
                v-model:search="genreSearch"
                label="Game Genre"
                :items="genreOptions"
                v-bind="inputProps"
                multiple
                chips
                closable-chips
                :clear-on-select="false"
                attach
                @update:search="fetchGenres"
            />
            <BaseInput
                v-model="description"
                label="Description"
                placeholder="Short description of the game"
                v-bind="inputProps"
            />
            <div v-for="row in numberFieldRows" :key="row[0].model" class="d-flex ga-3">
                <BaseInput
                    v-for="field in row"
                    :key="field.model"
                    v-model.number="numberFields[field.model]"
                    :label="field.label"
                    type="number"
                    v-bind="inputProps"
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
            <div class="d-flex justify-end ga-3">
                <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
                <v-btn color="primary" @click="handleConfirm" :loading="isLoading || isResolving">Add Game</v-btn>
            </div>
        </div>
    </BaseModal>
</template>
<script setup>
// Shared modal for adding a custom (not-yet-catalogued) board game.
//
// Two usages:
// - Profile / "Games Owned" flow (createOnly=false, default): calls addGame,
//   which creates the game AND adds it to the user's inventory in one call.
//   The caller (profile page) expects the raw InventoryUpdateResponse shape
//   ({ message, ownedGamesCount, games: [...] }) back on @confirm, exactly
//   as addGame returns it — no extra resolution needed here.
// - Library / rulebook-upload flow (createOnly=true): calls createGame,
//   which only creates the catalog entry and does NOT touch inventory. Its
//   response has no usable id/title, so we poll search for the newly
//   created game and emit that resolved object instead.
import { ref, reactive, onMounted, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useProfile } from '@/composables/useProfile'
import { useBoardGames } from '~/composables/useBoardGames'
import { userService } from '~/services/userService'
import { useDebounceFn } from '@vueuse/core'

const { addGame, createGame, isLoading, error } = useProfile();
const { games, searchGames } = useBoardGames();

const props = defineProps({
    createOnly: {
        type: Boolean,
        default: false
    }
})
const open = defineModel({ type: Boolean, default: false })
const emit = defineEmits(['confirm', 'back'])

const inputProps = {
    variant: 'outlined',
    density: 'compact',
    hideDetails: true
}

const title = ref('');
const description = ref('');
const genres = ref([])
const fileName = ref('');
const fileInput = ref(null);
const file = ref(null);
const isResolving = ref(false);
const genreOptions = ref([]);
const genreSearch = ref('')
const isSelecting = ref(false)

const numberFields = reactive({
    minPlayers: null,
    maxPlayers: null,
    minAge: null,
    duration: null
})

const numberFieldRows = [
    [
        { model: 'minPlayers', label: 'Min Players' },
        { model: 'maxPlayers', label: 'Max Players' }
    ],
    [
        { model: 'minAge', label: 'Minimum Age' },
        { model: 'duration', label: 'Duration (minutes)' }
    ]
]

watch(genreSearch, (val) => {
    if (isSelecting.value) {
        isSelecting.value = false;
        return;
    }
    if (val !== null && val !== undefined) {
        fetchGenres(val)
    }
})

const handleGenreSelect = () => {
    isSelecting.value = true
}

const fetchGenres = useDebounceFn(async (query) => {
    if (query === null || query === undefined) return; // ignore post-selection clear
    try {
        const res = await userService.getGenres(query);
        genreOptions.value = res.genres;
    }
    catch (err) {
        console.error('failed to load genres: ', err);
    }
}, 300)

onMounted(() => fetchGenres(''))

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
    const chosenFile = e.target.files[0];
    if (chosenFile) {
        file.value = chosenFile;
        fileName.value = chosenFile.name;
    }
}

const resetNumberFields = () => {
    numberFields.minPlayers = null;
    numberFields.maxPlayers = null;
    numberFields.minAge = null;
    numberFields.duration = null;
}

const closeModal = () => {
    open.value = false;
    title.value = '';
    description.value = '';
    resetNumberFields();
    genres.value = [];
    fileName.value = '';
    file.value = null;
}

// Newly created games can take a moment to become searchable, so poll
// for the game to actually exist before handing it back to the caller.
// Only used on the createOnly path — see note above.
const waitForCreatedGame = async (gameTitle) => {
    const maxAttempts = 4
    const delayMs = 500
    const normalizedTitle = gameTitle.trim().toLowerCase()

    try {
        for (let attempt = 1; attempt <= maxAttempts; attempt++) {
            await searchGames(gameTitle)

            const match = games.value.find(
                g => g.title?.trim().toLowerCase() === normalizedTitle
            )

            if (match) {
                return match
            }

            if (attempt < maxAttempts) {
                await new Promise(resolve => setTimeout(resolve, delayMs))
            }
        }

        console.error('Search returned no exact-title match for newly created game after retries:', gameTitle)
        return null
    } finally {
        games.value = []
    }
}

const handleConfirm = async () => {
    const { minPlayers, maxPlayers, minAge, duration } = numberFields;
    if (!title.value || !file.value || !description.value || !minPlayers || !maxPlayers || !minAge || !duration) return
    if (minPlayers >= maxPlayers) { // only cause it's in the backend
        console.error('minPlayers must be less than maxPlayers')
        return
    }
    const gameData = {
        title: title.value,
        description: description.value,
        minPlayers,
        maxPlayers,
        minAge,
        duration,
        genres: genres.value
    }
    try {
        if (props.createOnly) {
            const res = await createGame(gameData, file.value)

            isResolving.value = true
            const createdGame = await waitForCreatedGame(gameData.title)
            isResolving.value = false

            emit('confirm', createdGame ?? res, gameData)
        } else {
            const res = await addGame(gameData, file.value)
            emit('confirm', res, gameData)
        }

        closeModal();
    }
    catch (err) {
        isResolving.value = false
        console.error("error while adding game: ", err);
    }
}
</script>
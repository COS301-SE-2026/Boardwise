<template>
    <PageContainer>
        <div ref="pageRef" class="onboarding-page" :class="{ 'onboarding-page--wide': step > 1 }">

            <Welcome
                v-if="step === 1"
                :username="user?.username"
                @continue="step = 2"
                @skip="step = 4"
            />

            <OnboardingGenres
                v-else-if="step === 2"
                :genres="genreOptions"
                @continue="handleGenresSelected"
                @skip="step = 4"
            />

            <OnBoardingGames
                v-else-if="step === 3"
                :games="displayedGames"
                :selected-genres="pickedGenres"
                :is-submitting="isSubmitting"
                @select-tab="handleTabChange"
                @search="handleSearch"
                @continue="handleGamesSelected"
                @skip="step = 4"
                @back="step = 2"
            />

            <Complete
                v-else
                :games="selectedGames"
                @finished="router.push('/library')"
            />
        </div>
    </PageContainer>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'

import Complete from '~/components/features/auth/onboarding/Complete.vue'
import OnBoardingGames from '~/components/features/auth/onboarding/OnBoardingGames.vue'
import OnboardingGenres from "~/components/features/auth/onboarding/OnboardingGenres.vue"
import Welcome from '~/components/features/auth/onboarding/Welcome.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import { userService } from '~/services/userService'
import { useBoardGames } from '~/composables/useBoardGames'
// import { useProfile } from '~/composables/useProfile'

const router = useRouter()
const { user } = useAuth()
// const { updateGenrePreferences } = useProfile()

const pageRef = ref(null)
const step = ref(1)
const isSubmitting = ref(false)
const errorMessages = ref('')

const searchTerm = ref('')
const selectedGameIds = ref([])
const selectedGenreIds = ref([])
const genreOptions = ref([])
const seenGames = ref({})

const genres = ["Economic"," Strategy","Abstract Strategy","Card Game","Science Fiction","Cooperative", "Party Game", "Fantasy", "Wargame", "Dice"];

const {
    games,
    searchGames,
    searchGenres,
    getTopNGenresFromUsersPreferences,
    getPopularBoardgamesFromUserPrefrences,
    getPopularGamesBasedOnGenres,
    topGamesInDBBasedOnGenres
} = useBoardGames()

// remember every game we've shown, so the Complete step can find them
watch([topGamesInDBBasedOnGenres, games], ([top, found]) => {
    [...(top ?? []), ...(found ?? [])].forEach(g => { seenGames.value[g.id] = g })
}, { immediate: true, deep: true })

const displayedGames = computed(() =>
    searchTerm.value ? games.value : topGamesInDBBasedOnGenres.value
)

const pickedGenres = computed(() =>
    genreOptions.value.filter(g => selectedGenreIds.value.includes(g.id))
)

const selectedGames = computed(() =>
    selectedGameIds.value.map(id => seenGames.value[id]).filter(Boolean)
)

watch(step, async () => {
    await nextTick()
    pageRef.value?.querySelector('h1')?.focus()
})

onMounted(async () => {
    // if(localStorage.getItem("access_token")){
    //     router.push("/library");
    // }
    await Promise.all([
        loadTopNGenres(),
        handleGetGames(),
        getPopularBoardgamesFromUserPrefrences()
    ])

})

async function loadTopNGenres() {
    await getTopNGenresFromUsersPreferences(10)
    genreOptions.value = (genres ?? []).map(name => ({ id: name, label: name }))
}

async function handleGetGames() {
    await Promise.all([searchGames(), searchGenres()])
}

async function handleSearch(q) {
    searchTerm.value = q
    if (q) await searchGames(q)
}

async function handleTabChange(tab) {
    searchTerm.value = ''
    const genres = tab === 'all' ? selectedGenreIds.value : [tab]
    await getPopularGamesBasedOnGenres(genres)
}

async function handleGenresSelected(ids) {
    isSubmitting.value = true
    errorMessages.value = ''

    try{
        await userService.updateGenrePreferences(ids)
        await getPopularGamesBasedOnGenres(ids)
        selectedGenreIds.value = ids
        step.value = 3
    }
    catch (err) {
        console.error('Failed to save genre preferences or fetch games by genre: ', err)
        errorMessages.value = 'Failed to save genre preferences or fetch games by genre. Please try again.'
    } finally {
        isSubmitting.value = false
    }
    
}

async function handleGamesSelected(selectedIds) {
    isSubmitting.value = true
    errorMessages.value = ''

    try {
        await userService.addGamesToInventory({ knownGameIds: selectedIds })
        selectedGameIds.value = selectedIds
        step.value = 4
    } catch (err) {
        console.error('Failed to save game inventory: ', err)
        errorMessages.value = 'Failed to save your games. Please try again.'
    } finally {
        isSubmitting.value = false
    }
}
</script>
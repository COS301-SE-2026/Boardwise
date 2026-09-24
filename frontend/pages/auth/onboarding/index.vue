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
                :games="topGamesInDBBasedOnGenres"
                :selected-genres="pickedGenres"
                :is-submitting="isSubmitting"
                @select-tab="handleTabChange"
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


const router = useRouter()
const { user } = useAuth()

const pageRef = ref(null)
const step = ref(1)
const isSubmitting = ref(false)
const errorMessages = ref('')

const selectedGameIds = ref([])
const selectedGenreIds = ref([])

const { games, topUserGames, searchGames, searchGenres, getTopNGenresFromUsersPreferences, topNgenres, getPopularBoardgamesFromUserPrefrences, getPopularGamesBasedOnGenres,topGamesInDBBasedOnGenres   } = useBoardGames()

const seenGames = ref({});

watch(topGamesInDBBasedOnGenres, (list) => {
    (list ?? []).forEach(g => { seenGames.value[g.id] = g })
}, { immediate: true, deep: true })

// TODO: Make the genre selection according to the popularity
const genreOptions = ref([])

async function loadTopNGenres(){// Based on the how many users have this set as a preference
    await getTopNGenresFromUsersPreferences(10);
    genreOptions.value = (topNgenres.value?? []).map(name=>({id:name, label: name}));
}

const pickedGenres = computed(() => 
    genreOptions.value.filter(g => selectedGenreIds.value.includes(g.id))
)

watch(step, async () => {
    await nextTick()
    pageRef.value?.querySelector('h1')?.focus()
})

// TODO: Make the games a selection of popularity or based on genres
onMounted(async () => {
    await Promise.all([
        loadTopNGenres(),
        handleGetGames(),
        getPopularBoardgamesFromUserPrefrences()
    ]);
})

async function handleGetGames(){
    await Promise.all([searchGames(), searchGenres()])
}

async function handleTabChange(tab) {
    const genres = tab === 'all' ? selectedGenreIds.value : [tab]
    await getPopularGamesBasedOnGenres(genres)
}

const selectedGames = computed (()=> selectedGameIds.value.map(id=> seenGames.value[id]).filter(Boolean))
async function handleGenresSelected(ids) {
    selectedGenreIds.value = ids
    await getPopularGamesBasedOnGenres(ids);
    step.value = 3
}

async function handleGamesSelected(selectedIds) {
    isSubmitting.value = true
    errorMessages.value = ''

    try{
        await userService.addGamesToInventory({ knownGameIds: selectedIds})
        selectedGameIds.value = selectedIds
        step.value = 4
    }catch(err){
        console.error('Failed to save game inventory: ', err);
        errorMessages.value = 'Failed to save your games. Please try again.'
    }finally{
        isSubmitting.value = false
    }
}
</script>
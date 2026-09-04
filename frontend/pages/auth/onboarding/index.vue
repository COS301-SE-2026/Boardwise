<template>
    <PageContainer>
        <div class="onboarding-page">
            <Welcome
                v-if="step === 1"
                :username="user?.username"
                @continue="step = 2"
            />

            <OnBoardingGames
                v-else-if="step === 2"
                :games="games"
                @continue="handleGamesSelected"
                @skip="step = 3"
            />

            <Complete
                v-else
                @finished="router.push('/library')"
            />
        </div>
    </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import Complete from '~/components/features/auth/onboarding/Complete.vue';
import OnBoardingGames from '~/components/features/auth/onboarding/OnBoardingGames.vue';
import Welcome from '~/components/features/auth/onboarding/Welcome.vue';
import PageContainer from '~/components/layout/PageContainer.vue';
import { userService } from '~/services/userService';

const router = useRouter()
const { user } = useAuth()

const step = ref(1)
const isSubmitting = ref(false)
const errorMessages = ref('')

const { games, searchGames } = useBoardGames()

onMounted(() => {
    handleGetGames()
})

async function handleGetGames(){
    try{
        await searchGames();
        console.log("Games Array: ", games.value)
    }catch(err){
        console.error('Failed to fetch boardgames: ', err);
        errorMessages.value = 'Failed to fetch games'
    }
}

async function handleGamesSelected(selectedIds) {
    isSubmitting.value = true
    errorMessages.value = ''

    try{
        await userService.addGamesToInventory({ knownGameIds: selectedIds})
        step.value = 3
    }catch(err){
        console.error('Failed to save game inventory: ', err);
        errorMessages.value = 'Failed to save your games. Please try again.'
    }finally{
        isSubmitting.value = false
    }
}
</script>

<style scoped>
.onboarding-page {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: calc(100vh - 80px);
    padding: 4rem 1.5rem;
    background: var(--color-surface);
}
</style>
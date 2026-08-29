<template>
    <PageContainer class="onboarding-page d-flex align-center justify-center">
        <Welcome
            v-if="step === 1"
            :username="user?.username"
            @continue="step = 2"
        />

        <OnBoardingGames
            v-else-if="step === 2"
            :games="availableGames"
            @continue="handleGamesSelected"
        />

        <Complete
            v-else
            @finished="routerKey.push('/library')"
        />
    </PageContainer>
</template>

<script setup>
import { ref } from 'vue'

import Complete from '~/components/features/auth/onboarding/Complete.vue';
import OnBoardingGames from '~/components/features/auth/onboarding/OnBoardingGames.vue';
import Welcome from '~/components/features/auth/onboarding/Welcome.vue';
import PageContainer from '~/components/layout/PageContainer.vue';

const router = useRouter()
const { user } = useAuth()
const step = ref(1)

// Todo: replace with real fetch from game catalog
const availableGames = ref([])

async function handleGamesSelected(selectedIds) {
    // Todo: persist selectedIds to user's collection via API
    step.value = 3
}
</script>

<style scoped>
.onboarding-page {
    min-height: calc(100vh - 80px);
    padding: 4rem 1.5rem
}
</style>
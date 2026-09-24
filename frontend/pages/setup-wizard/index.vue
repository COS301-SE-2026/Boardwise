<template>
    <PageContainer>
        <Navbar />

        <div class="setup-hub-hero"> 
            <BaseBackButton to="/library">Back to Library</BaseBackButton>

            <span class="setup-hub__eyebrow">
                <span class="setup-hub__eyebrow-dot" />
                RAG-Powered Tabletop Setup Wizard
            </span>

            <SectionTitle 
                title="Setup Wizard Hub"
                subtitle="Get from unboxing to first roll in minutes, with step-by-step guidance grounded in official rulebooks."
            />

            <BaseSearch
                v-model="searchQuery"
                placeholder="Search your library or find a setup guide..."
                aria-label="Search setup guides"
                class="setup-hub__search"
            />
        </div>

        <div class="active-banner">
            <ActiveSetupBanner
                v-if="activeSetup"
                :game="activeSetup"
                @resume="goToWizard(activeSetup.id)"
                @restart="restartSetup"
            />
        </div>

        <div class="section">
            <div class="page-header">
                <div>
                    <h2 class="section-title__heading" style="font-size: var(--fs-h2);">
                        Wizard-Ready Games
                    </h2>

                    <p class="card-meta">Grounded rulebooks loaded with full setup guidance.</p>
                </div>

                <span class="card-meta">Showing {{  games.length  }} games</span>
            </div>

            <BaseLoadingState v-if="isLoading" message="Loading your library..." />

            <BaseErrorState
                v-else-if="error"
                :message="error"
                retryable
                @retry="() => searchGames(searchQuery)"
            />

            <BaseEmptyState
                v-else-if="!games.length"
                title="No games found"
                :message="searchQuery ? `Nothing matched \u201C${searchQuery}\u201D.` : 'Add games to your library to get setup guidance.'"
            >
                <template v-if="searchQuery" #actions>
                    <BaseButton variant="secondary" @click="searchQuery = ''">Clear search</BaseButton>
                </template>
            </BaseEmptyState>

            <BaseGrid v-else cols="320px" gap="24px">
                <SetupGameCard v-for="game in games" :key="game.id" :game="game" @launch="goToWizard" />
            </BaseGrid>
        </div>

        <div class="section setup-hub__how">
                <div class="setup-hub__how-header">
                <span class="setup-hub__eyebrow setup-hub__eyebrow--muted">Zero Rulebook Headaches</span>
                <h2 class="section-title__heading">How Boarley RAG Setup Works</h2>
            </div>

            <BaseGrid cols="280px" gap="24px">
                <BaseCard v-for="step in howItWorks" :key="step.title">
                    <div class="setup-hub__how-number">{{ step.number }}</div>
                    <h3 class="card-title">{{ step.title }}</h3>
                    <p class="card-meta">{{ step.body }}</p>
                </BaseCard>
            </BaseGrid>
        </div>

    </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'
import BaseErrorState from '~/components/ui/BaseErrorState.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import SetupGameCard from '~/components/features/setup-wizard/SetupGameCard.vue'
import ActiveSetupBanner from '~/components/features/setup-wizard/ActiveSetupBanner.vue'
import { useActiveSetup } from '~/composables/useSetupWizard'

const howItWorks = [
  { number: 1, title: 'Physical Board Guidance', body: 'Interactive diagrams show exactly where every tile, card deck, and token sits.' },
  { number: 2, title: 'Micro-Task Checklists', body: 'Verify each phase in seconds with zero rulebook flipping.' },
  { number: 3, title: 'Instant Grounded RAG', body: 'Ask natural questions and get exact citations from the official rulebook.' }
]

const router = useRouter()
const { games, isLoading, error, searchGames } = useBoardGames()
const { activeSetup, restartSetup } = useActiveSetup()

const searchQuery = ref('')

onMounted(() => searchGames())

const delaySearch = useDebounceFn((q: string) => searchGames(q), 400)
watch(searchQuery, (q) => delaySearch(q))

const goToWizard = (gameOrId: any) => {
    const id = typeof gameOrId === 'string' ? gameOrId : gameOrId.id 
    router.push(`/setup-wizard/${id}`)
}
</script>
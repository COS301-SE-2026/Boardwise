<template>
    <PageContainer>
        <Navbar />

        <div class="wizard-hub">
            <div class="wizard-hub__crumb">
                <BaseBackButton to="/library">Back to Library</BaseBackButton>
                <span class="wizard-hub__divider">/</span>
                <span class="wizard-hub__game">{{  gameTitle  }}</span>
            </div>

            <div class="wizard-hub__progress">
                <span class="card-meta">Step {{ stepNumber }} of {{  totalSteps }}</span>

                <div class="wizard-hub__track">
                    <div class="wizard-hub__fill" :style="{ width: (stepNumber / totalSteps) * 100 + '%'}" />
                </div>
            </div>
        </div>

        <BaseLoadingState v-if="isLoadingGame" message="Loading setup guide..." />
        

        <div v-else class="wizard-layout">
            <div class="wizard-layout__main">
                <div class="wizard-layout__heading">
                    <div>
                        <span class="wizard-layout__eyebrow">Phase 1: Inventory &amp; Preparation</span>
                        <h1 class="page-header__title" style="font-size: var(--fs-h2);">{{ currentStep?.title }}</h1>
                        <p class="card-meta">{{ currentStep?.description }}</p>
                    </div>

                    <div class="wizard-layout__confirm">
                        <BaseBadge :variant="allConfirmed ? 'success' : 'neutral'">
                            {{ checkedCount }} of {{ checklist.length }} confirmed
                        </BaseBadge>

                        <BaseButton variant="text" size="small" @click="toggleAll">
                            {{  allConfirmed ? 'Uncheck All': 'Mark all verified' }}
                        </BaseButton>
                    </div>
                </div>

                <div class="wizard-layout__checklist">
                    <ChecklistItemCard v-for="item in checklist" :key="item.id" :item="item" @toggle="toggleItem" />
                </div>

                <div class="wizard-layou__hint">
                    <v-icon size="20" color="var(--color-text-muted)">mdi-help-circle-outline</v-icon>

                    <p class="card-meta">
                        <strong>Missing a piece?</strong> 
                        Ask Boarley in the sidebar for official replacement rulings.
                    </p>
                </div>
            </div>

            <WizardChatSidebar class="wizard-layour__sidebar" :game-title="gameTitle" />
        </div>

        <div class="wizard-footer">
            <NuxtLink to="/setup-wizard" class="wizard-footer__cancel">Cancel Setup</NuxtLink>
            <div class="wizard-footer__actions">
                <span class="card-meta">
                    {{ allConfirmed ? 'All pieces ready! Proceed when set.' : `${checklist.length - checkedCount} items left to verify.` }}
                </span>

                <BaseButton @click="handleNext">
                    {{ stepNumber < totalSteps ? `Next: Step ${stepNumber + 1}` : 'Complete Setup' }}
                </BaseButton>
            </div>
        </div>

        <BaseModal v-model="showConfirmModal" title="Some items aren't confirmed" aria-label="Confirm proceeding with unchecked items">
            <p>You still have {{ checklist.length - checkedCount }} unconfirmed item(s). Continue anyway?</p>
            <template #actions>
                <BaseButton variant="secondary" @click="showConfirmModal = false">Go back</BaseButton>
                <BaseButton @click="confirmNext">Continue</BaseButton>
            </template>
        </BaseModal>
    </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

import ChecklistItemCard from '~/components/features/setup-wizard/ChecklistItemCard.vue'
import WizardChatSidebar from '~/components/features/setup-wizard/WizardChatSidebar.vue'

import { useBoardGames } from '~/composables/useBoardGames'
import { useSetupChecklist, useActiveSetup } from '~/composables/useSetupWizard'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

const route = useRoute()
const gameId = route.params.id as string

// TODO: replace with getGameById lookup

const { games, isLoading: isLoadingGame, searchGames } = useBoardGames()
const { checklist, stepNumber, totalSteps, currentStep, checkedCount, allConfirmed, toggleItem, toggleAll, nextStep } = useSetupChecklist()
const { setActiveSetup, clearActiveSetup } = useActiveSetup()

const showConfirmModal = ref(false)

const game = computed(() => games.value.find((g: any) => String(g.id) === gameId))
const gameTitle = computed(() => game.value?.title || 'Setup Guide')

onMounted(async () => {
    if (!games.value.length) await searchGames()
})

const persistProgress = () => {
    setActiveSetup({
        id: gameId, 
        title: gameTitle.value,
        coverImage: game.value?.imageUrl,
        step: stepNumber.value, 
        totalSteps
    })
}

const handleNext = () => {
    if (!allConfirmed.value && stepNumber.value === 1) {
        showConfirmModal.value = true
        return
    }

    advance()
}

const confirmNext = () => {
    showConfirmModal.value = false
    advance()
}

const advance = () => {
    if (stepNumber.value < totalSteps) {
        nextStep()
        persistProgress()
    } else {
        clearActiveSetup()
        // TODO: no "setup complete" state designed yet
    }
}
</script>
<template>
    <PageContainer>
        <Navbar />

        <div class="wizard-hub">
            <div class="wizard-hub__crumb">
                <BaseBackButton to="/setup-wizard">Back to Wizard Hud</BaseBackButton>
                <span class="wizard-hub__divider">/</span>
                <span class="wizard-hub__game">{{  gameTitle  }}</span>
            </div>

            <div class="wizard-hub__progress">
                <span class="card-meta">Step {{ stepNumber }} of {{  totalSteps }}</span>

                <div class="wizard-hub__track">
                    <div class="wizard-hub__fill" :style="{ width: (totalSteps?(stepNumber / totalSteps) * 100: 0) + '%'}" />
                </div>
            </div>
        </div>

        <BaseLoadingState v-if="showLoading"
         :message=" isGenerating? `Generating setup guide... ${progress}%`: 'Loading setup guide...'" />
        
        <div v-else-if="error" class ="wizard-layout__main">
            <p class="card-meta">{{ error }}</p>
            <BaseButton @click="start(rulebookId)"> Try again </BaseButton>

        </div>

        <div v-else class="wizard-layout">
            <div class="wizard-layout__main">
                <div class="wizard-layout__heading">
                    <div>
                        <span class="wizard-layout__eyebrow">{{ currentStep?.phase }}</span>
                        <h1 class="page-header__title" style="font-size: var(--fs-h2);">{{ currentStep?.title }}</h1>
                        <p class="card-meta">{{ currentStep?.description }}</p>
                    </div>

                    <div v-if="stepChecklist.length" class="wizard-layout__confirm">
                        <BaseBadge :variant="allConfirmed ? 'success' : 'neutral'">
                            {{ checkedCount }} of {{ stepChecklist.length }} confirmed
                        </BaseBadge>

                        <BaseButton variant="text" size="small" @click="toggleAll">
                            {{  allConfirmed ? 'Uncheck All': 'Mark all verified' }}
                        </BaseButton>
                    </div>
                </div>

                <div v-if="stepChecklist.length" class="wizard-layout__checklist">
                    <ChecklistItemCard v-for="item in stepChecklist" :key="item.id" :item="item" @toggle="toggleItem" />
                </div>

                <div v-if="stepChecklist.length"class="wizard-layout__hint">
                    <v-icon size="20" color="var(--color-text-muted)">mdi-help-circle-outline</v-icon>

                    <p class="card-meta">
                        <strong>Missing a piece?</strong> 
                        Ask Boarley in the sidebar for official replacement rulings.
                    </p>
                </div>
            </div>

            <WizardChatSidebar class="wizard-layout__sidebar" :game-title="gameTitle" />
        </div>

        <div v-if="!showLoading && !error" class="wizard-footer">
            <NuxtLink to="/setup-wizard" class="wizard-footer__cancel">Cancel Setup</NuxtLink>
            <div class="wizard-footer__actions">
                <span class="card-meta">
                    {{ !stepChecklist.length ? 'Read the rule, then continue.' :allConfirmed ? 'All pieces ready! Proceed when set.' : `${stepChecklist.length - checkedCount} items left to verify.` }}
                </span>

                <BaseButton @click="handleNext">
                    {{ stepNumber < totalSteps ? `Next: Step ${stepNumber + 1}` : 'Complete Setup' }}
                </BaseButton>
            </div>
        </div>

        <SetupCompleteModal
            v-model="showCompleteModal"
            :game-title="gameTitle"
            @finish="finishSetup"
        />

        <BaseModal v-model="showConfirmModal" title="Some items aren't confirmed" aria-label="Confirm proceeding with unchecked items">
            <p>You still have {{ stepChecklist.length - checkedCount }} unconfirmed item(s). Continue anyway?</p>
            <template #actions>
                <BaseButton variant="secondary" @click="showConfirmModal = false">Go back</BaseButton>
                <BaseButton @click="confirmNext">Continue</BaseButton>
            </template>
        </BaseModal>
    </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

import ChecklistItemCard from '~/components/features/setup-wizard/ChecklistItemCard.vue'
import WizardChatSidebar from '~/components/features/setup-wizard/WizardChatSidebar.vue'
import SetupCompleteModal from '~/components/features/setup-wizard/SetupCompleteModal.vue'

import { useBoardGames } from '~/composables/useBoardGames'
import {useSetupWizard, useSetupChecklist, useActiveSetup } from '~/composables/useSetupWizard'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

const route = useRoute()
const router = useRouter()
const rulebookId = route.params.id as string

// TODO: replace with getGameById lookup
const showLoading = computed(()=> isLoadingGame.value || isGenerating.value || (!wizard.value && !error.value))

const { games, isLoading: isLoadingGame, searchGames } = useBoardGames()
const { wizard, isLoading: isGenerating, progress, error, start } = useSetupWizard()
const {
    stepChecklist, stepNumber, totalSteps, currentStep,
    checkedCount, allConfirmed, toggleItem, toggleAll, nextStep,
} = useSetupChecklist(wizard)

const { setActiveSetup, clearActiveSetup } = useActiveSetup()

const showConfirmModal = ref(false)
const showCompleteModal = ref(false)

const game = computed(() => games.value.find((g: any) => g.rulebookId === rulebookId))
const gameTitle = computed(() => game.value?.title || 'Setup Guide')

onMounted(async () => {
    if (!games.value.length) await searchGames()
    await start(rulebookId)
})

const persistProgress = () => {
    setActiveSetup({
        id: rulebookId, 
        title: gameTitle.value,
        coverImage: game.value?.imageUrl,
        step: stepNumber.value, 
        totalSteps: totalSteps.value
    })
}

const handleNext = () => {
    if (!allConfirmed.value) {
        showConfirmModal.value = true
        return
    }

    advance()
}

const confirmNext = () => {
    showConfirmModal.value = false
    advance()
}

const finishSetup = () => {
    showCompleteModal.value = false
    clearActiveSetup()
    router.push('/setup-wizard')
}

const advance = () => {
    if (stepNumber.value < totalSteps.value) {
        nextStep()
        persistProgress()
    } else {
        showCompleteModal.value = true
    }
}
</script>
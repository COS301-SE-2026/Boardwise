<template>
  <PageContainer>
    <Navbar />

    <main class="game-architect-page">
      <BaseButton
        variant="text"
        prepend-icon="mdi-arrow-left"
        class="game-architect-page__back"
        @click="returnToSource"
      >
        Back to {{ sourceLabel }}
      </BaseButton>

      <div class="game-architect-shell">
        <GameArchitectProgress
          v-if="step <= 3"
          :current-step="step"
        />

        <GameArchitectActionStep
          v-if="step === 1"
          :model-value="mode"
          @update:model-value="chooseMode"
        />

        <GameArchitectGameStep
          v-else-if="step === 2"
          :games="games"
          :selected-games="selectedGames"
          :scale-mode="isScaleMode"
          :surprise-me="surpriseMe"
          :max-selected="maxSelectedGames"
          :loading="loadingGames"
          @toggle="toggleGame"
          @surprise="chooseSurpriseMe"
        />

        <GameArchitectReviewStep
          v-else-if="step === 3 && mode"
          v-model="prompt"
          v-model:scale-direction="scaleDirection"
          v-model:difficulty="difficulty"
          v-model:target-player-count="targetPlayerCount"
          :mode="mode"
          :selected-games="selectedGames"
          :surprise-me="surpriseMe"
        />

        <GameArchitectResult
          v-else-if="step === 4 && result"
          :result="result"
          @restart="reset"
        />

        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mt-6"
          role="alert"
          closable
          @click:close="error = ''"
        >
          {{ error }}
        </v-alert>

        <div v-if="step <= 3" class="game-architect-page__actions">
          <BaseButton
            v-if="step > 1"
            variant="secondary"
            prepend-icon="mdi-arrow-left"
            :disabled="generating"
            @click="back"
          >
            Previous
          </BaseButton>
          <span v-else />

          <BaseButton
            v-if="step < 3"
            append-icon="mdi-arrow-right"
            :disabled="!canContinue"
            @click="next"
          >
            Continue
          </BaseButton>

          <BaseButton
            v-else
            prepend-icon="mdi-creation-outline"
            :loading="generating"
            :disabled="!canContinue || generating"
            @click="generate"
          >
            Generate game concept
          </BaseButton>
        </div>
      </div>
    </main>
  </PageContainer>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import GameArchitectActionStep from '~/components/features/game-architect/GameArchitectActionStep.vue'
import GameArchitectGameStep from '~/components/features/game-architect/GameArchitectGameStep.vue'
import GameArchitectProgress from '~/components/features/game-architect/GameArchitectProgress.vue'
import GameArchitectResult from '~/components/features/game-architect/GameArchitectResult.vue'
import GameArchitectReviewStep from '~/components/features/game-architect/GameArchitectReviewStep.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useGameArchitect } from '~/composables/useGameArchitect'

const route = useRoute()
const router = useRouter()

const {
  step,
  mode,
  games,
  selectedGames,
  prompt,
  scaleDirection,
  difficulty,
  targetPlayerCount,
  surpriseMe,
  loadingGames,
  generating,
  error,
  result,
  maxSelectedGames,
  isScaleMode,
  canContinue,
  loadOwnedGames,
  chooseMode,
  toggleGame,
  chooseSurpriseMe,
  next,
  back,
  generate,
  reset
} = useGameArchitect()

const source = computed(() =>
  route.query.from === 'library' ? 'library' : 'profile'
)

const sourceLabel = computed(() =>
  source.value === 'library' ? 'Library' : 'Profile'
)

const returnToSource = () => router.push(`/${source.value}`)

onMounted(loadOwnedGames)
</script>

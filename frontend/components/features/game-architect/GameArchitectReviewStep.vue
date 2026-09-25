<template>
  <section aria-labelledby="architect-review-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 3 of 3</p>

      <h1 id="architect-review-title">
        Choose your game settings
      </h1>

      <p>
        Select the player range and difficulty for the generated game.
      </p>
    </div>

    <div class="game-architect-review-layout">
      <BaseCard class="game-architect-brief-card pa-6">
        <div v-if="mode === 'scale'" class="game-architect-settings">
        <v-radio-group
          :model-value="playerRange"
          label="How many players should the game support?"
          @update:model-value="updatePlayerRange"
        >
          <v-radio label="1–2 players" value="1-2" />
          <v-radio label="3–4 players" value="3-4" />
          <v-radio label="5–6 players" value="5-6" />
          <v-radio label="7 or more players" value="7+" />
        </v-radio-group>

        <v-radio-group
          :model-value="difficulty"
          label="Choose the game difficulty"
          @update:model-value="updateDifficulty"
        >
          <v-radio label="Easier" value="easier" />
          <v-radio label="Keep it similar" value="same" />
          <v-radio label="Harder" value="harder" />
        </v-radio-group>

        <v-radio-group
          v-if="mode === 'scale'"
          :model-value="scaleDirection"
          label="How should the game scale?"
          @update:model-value="updateScaleDirection"
        >
          <v-radio label="Scale up" value="up" />
          <v-radio label="Scale down" value="down" />
        </v-radio-group>
        </div>

        <div v-else class="game-architect-create-confirmation">
          <v-icon
            icon="mdi-sparkles"
            color="primary"
            size="48"
            aria-hidden="true"
          />

          <div>
            <h2>Ready to create</h2>
            <p>
              Boarley will create a new experience using your selected inspiration.
            </p>
          </div>
    </div>
      </BaseCard>

      <aside
        class="game-architect-review-summary"
        aria-label="Game settings summary"
      >
        <h2>Review</h2>

        <dl>
          <div>
            <dt>Action</dt>
            <dd>{{ modeLabel }}</dd>
          </div>

          <div>
            <dt>Inspiration</dt>
            <dd v-if="surpriseMe">Surprise me</dd>
            <dd v-else>{{ selectedGamesLabel }}</dd>
          </div>

          <div>
            <dt>Difficulty</dt>
            <dd>{{ difficultyLabel }}</dd>
          </div>

          <div>
            <dt>Player range</dt>
            <dd>{{ playerRangeLabel }}</dd>
          </div>

          <div v-if="mode === 'scale'">
            <dt>Scale direction</dt>
            <dd>{{ scaleDirectionLabel }}</dd>
          </div>
        </dl>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, type PropType } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import type {
  GameArchitectGame,
  GameArchitectMode,
  GameDifficulty,
  PlayerRange,
  ScaleDirection
} from '~/services/gameArchitectService'

const props = defineProps({
  mode: {
    type: String as PropType<GameArchitectMode>,
    required: true
  },
  selectedGames: {
    type: Array as PropType<GameArchitectGame[]>,
    default: () => []
  },
  surpriseMe: {
    type: Boolean,
    default: false
  },
  scaleDirection: {
    type: String as PropType<ScaleDirection | null>,
    default: null
  },
  difficulty: {
    type: String as PropType<GameDifficulty | null>,
    default: null
  },
  playerRange: {
    type: String as PropType<PlayerRange | null>,
    default: null
  }
})

const emit = defineEmits<{
  'update:scaleDirection': [value: ScaleDirection]
  'update:difficulty': [value: GameDifficulty]
  'update:playerRange': [value: PlayerRange]
}>()

const updateScaleDirection = (value: unknown) => {
  emit('update:scaleDirection', value as ScaleDirection)
}

const updateDifficulty = (value: unknown) => {
  emit('update:difficulty', value as GameDifficulty)
}

const updatePlayerRange = (value: unknown) => {
  emit('update:playerRange', value as PlayerRange)
}

const modeLabel = computed(() =>
  props.mode === 'scale'
    ? 'Scale an existing game'
    : 'Create a new experience'
)

const selectedGamesLabel = computed(() => {
  const titles = props.selectedGames.map(game => game.title)

  return titles.length > 0
    ? titles.join(', ')
    : 'No games selected'
})

const difficultyLabel = computed(() => {
  const labels: Record<GameDifficulty, string> = {
    easier: 'Easier',
    same: 'Keep it similar',
    harder: 'Harder'
  }

  return props.difficulty
    ? labels[props.difficulty]
    : 'Not selected'
})

const playerRangeLabel = computed(() => {
  const labels: Record<PlayerRange, string> = {
    '1-2': '1–2 players',
    '3-4': '3–4 players',
    '5-6': '5–6 players',
    '7+': '7 or more players'
  }

  return props.playerRange
    ? labels[props.playerRange]
    : 'Not selected'
})

const scaleDirectionLabel = computed(() => {
  if (props.scaleDirection === 'up') return 'Scale up'
  if (props.scaleDirection === 'down') return 'Scale down'

  return 'Not selected'
})
</script>
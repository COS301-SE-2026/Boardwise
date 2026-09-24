<template>
  <section aria-labelledby="architect-review-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 3 of 3</p>
      <h1 id="architect-review-title">Describe your game idea</h1>
      <p>
        Give Boarley direction about the audience, theme, difficulty, or change
        you want to make.
      </p>
    </div>

    <div class="game-architect-review-layout">
      <BaseCard class="game-architect-brief-card pa-6">
        <div v-if="mode === 'scale'">
          <v-radio-group
            :model-value="scaleDirection"
            label="How should the player count change?"
            @update:model-value="updateScaleDirection"
          >
            <v-radio label="Scale up for more players" value="up" />
            <v-radio label="Scale down for fewer players" value="down" />
          </v-radio-group>

          <v-radio-group
            :model-value="difficulty"
            label="How should the difficulty change?"
            @update:model-value="updateDifficulty"
          >
            <v-radio label="Make it easier" value="easier" />
            <v-radio label="Keep it similar" value="same" />
            <v-radio label="Make it harder" value="harder" />
          </v-radio-group>

          <v-text-field
            :model-value="targetPlayerCount"
            label="Target player count (optional)"
            type="number"
            min="1"
            max="20"
            variant="outlined"
            rounded="lg"
            hide-details="auto"
            @update:model-value="updateTargetPlayerCount"
          />
        </div>

        <template v-else>
          <BaseTextArea
            :model-value="modelValue"
            label="Blueprint notes"
            :placeholder="placeholder"
            :rows="8"
            maxlength="1000"
            counter
            @update:model-value="emit('update:modelValue', $event)"
          />

          <p class="game-architect-field-help">
            Use at least 10 characters. Do not include private information.
          </p>
        </template>
      </BaseCard>

      <aside class="game-architect-review-summary" aria-label="Game brief summary">
        <h2>Review</h2>
        <dl>
          <div>
            <dt>Action</dt>
            <dd>{{ modeLabel }}</dd>
          </div>
          <div>
            <dt>Inspiration</dt>
            <dd v-if="surpriseMe">Surprise me</dd>
            <dd v-else>{{ selectedGames.map(game => game.title).join(', ') }}</dd>
          </div>
        </dl>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, type PropType } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import type {
  GameArchitectGame,
  GameArchitectMode,
  GameDifficulty,
  ScaleDirection
} from '~/services/gameArchitectService'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
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
  targetPlayerCount: {
    type: Number as PropType<number | null>,
    default: null
  }
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:scaleDirection': [value: ScaleDirection]
  'update:difficulty': [value: GameDifficulty]
  'update:targetPlayerCount': [value: number | null]
}>()

const updateScaleDirection = (value: unknown) => {
  emit('update:scaleDirection', value as ScaleDirection)
}

const updateDifficulty = (value: unknown) => {
  emit('update:difficulty', value as GameDifficulty)
}

const updateTargetPlayerCount = (value: unknown) => {
  const parsedValue = Number(value)

  emit(
    'update:targetPlayerCount',
    Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
  )
}

const modeLabel = computed(() =>
  props.mode === 'scale'
    ? 'Scale an existing game'
    : 'Create a new experience'
)

const placeholder = computed(() =>
  props.mode === 'scale'
    ? 'Example: Adapt this game for six players while keeping turns under five minutes…'
    : 'Example: Create a cooperative mystery game for families that takes about 45 minutes…'
)
</script>

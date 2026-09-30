<template>
  <section aria-labelledby="architect-review-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 3 of 3</p>

      <h1 id="architect-review-title">
        {{
          mode === 'scale'
            ? 'Choose how to scale your game'
            : 'Choose your game settings'
        }}
      </h1>

      <p v-if="mode === 'scale'">
        Choose whether Boarley should scale the selected game up or down.
      </p>

      <p v-else>
        Select the player range and difficulty for your new experience.
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
          <v-radio label="Easier — reduce complexity" value="easier" />
          <v-radio label="Keep it similar  — preserve complexity" value="same" />
          <v-radio label="Harder — increase complexity" value="harder" />
        </v-radio-group>

        <v-alert
            v-if="difficulty || playerRange"
            type="info"
            variant="tonal"
            class="game-architect-scaling-impact mt-4"
            aria-live="polite"
          >
            <strong>Scaling impact</strong>

            <p v-if="difficulty" class="mt-2 mb-1">
              {{ difficultyImpact }}
            </p>

            <p v-if="playerRange" class="mb-0">
              Boarley will adapt the rules and components to support
              {{ playerRangeLabel.toLowerCase() }}.
            </p>
          </v-alert>
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

          <div v-if="mode === 'scale'">
            <dt>Difficulty</dt>
            <dd>{{ difficultyLabel }}</dd>
          </div>

          <div v-if="mode === 'scale'">
            <dt>Player range</dt>
            <dd>{{ playerRangeLabel }}</dd>
          </div>

        </dl>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
  mode: {
    type: String,
    required: true
  },
  selectedGames: {
    type: Array,
    default: () => []
  },
  surpriseMe: {
    type: Boolean,
    default: false
  },
  difficulty: {
    type: String,
    default: null
  },
  playerRange: {
    type: String,
    default: null
  },
  maxSelected: {
  type: Number,
  default: 2
}
})

const emit = defineEmits([
  'update:difficulty',
  'update:playerRange'
])

const updateDifficulty = value => {
  emit('update:difficulty', value )
}

const updatePlayerRange = value => {
  emit('update:playerRange', value)
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
  const labels = {
    easier: 'Easier',
    same: 'Keep it similar',
    harder: 'Harder'
  }

  return props.difficulty
    ? labels[props.difficulty]
    : 'Not selected'
})

const difficultyImpact = computed(() => {
  if (props.difficulty === 'easier') {
    return 'Complexity will scale down with simpler rules and decisions.'
  }

  if (props.difficulty === 'harder') {
    return 'Complexity will scale up with deeper rules and decisions.'
  }

  if (props.difficulty === 'same') {
    return 'The current level of complexity will be preserved.'
  }

  return ''
})
const playerRangeLabel = computed(() => {
  const labels = {
    '1-2': '1–2 players',
    '3-4': '3–4 players',
    '5-6': '5–6 players',
    '7+': '7 or more players'
  }

  return props.playerRange
    ? labels[props.playerRange]
    : 'Not selected'
})

</script>
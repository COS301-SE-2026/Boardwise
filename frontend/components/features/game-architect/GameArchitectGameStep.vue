<template>
  <section aria-labelledby="architect-games-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 2 of 3</p>
      <h1 id="architect-games-title">
        {{ scaleMode ? 'Select a game to scale' : 'Select games for inspiration' }}
      </h1>
      <p v-if="scaleMode">Choose one game from your collection.</p>
      <p v-else>
        Choose up to {{ maxSelected }} games, or let Boarley surprise you.
      </p>
    </div>

    <div class="game-architect-selection-layout">
      <div class="game-architect-selection-layout__main">
        <BaseSearch
          v-model="search"
          aria-label="Search your game collection"
          placeholder="Search your collection..."
          class="mb-5"
        />

        <output
          v-if="loading"
          class="game-architect-loading"
          aria-live="polite"
        >
          <v-progress-circular indeterminate color="primary" size="42" />
          <span>Loading your games…</span>
        </output>

        <BaseEmptyState
          v-else-if="!filteredGames.length"
          title="No games found"
          :message="emptyMessage"
          icon="mdi-dice-multiple-outline"
        />

        <div v-else class="game-architect-game-grid">
          <button
            v-for="game in filteredGames"
            :key="game.id"
            type="button"
            class="game-architect-game-card"
            :class="{
              'game-architect-game-card--selected': isSelected(game.id),
              'game-architect-game-card--disabled': selectionDisabled(game.id)
            }"
            :aria-pressed="isSelected(game.id)"
            :disabled="selectionDisabled(game.id)"
            @click="emit('toggle', game)"
          >
            <BaseImage
              :src="game.imageUrl"
              :alt="`${game.title} cover`"
              height="152px"
            />
            <span class="game-architect-game-card__content">
              <strong>{{ game.title }}</strong>
              <span>{{ game.genres.slice(0, 2).join(' · ') || 'Board game' }}</span>
            </span>
            <v-icon
              v-if="isSelected(game.id)"
              class="game-architect-game-card__check"
              icon="mdi-check-circle"
              color="primary"
              aria-hidden="true"
            />
          </button>
        </div>
      </div>

      <aside class="game-architect-selection-summary" aria-label="Current selection">
        <h2>Your selection</h2>

        <button
          v-if="!scaleMode"
          type="button"
          class="game-architect-surprise"
          :class="{ 'game-architect-surprise--selected': surpriseMe }"
          :aria-pressed="surpriseMe"
          @click="emit('surprise')"
        >
          <v-icon icon="mdi-dice-multiple" size="30" aria-hidden="true" />
          <span>
            <strong>Surprise me</strong>
            <small>Boarley will choose the inspiration.</small>
          </span>
        </button>

        <ul v-if="selectedGames.length" class="game-architect-selected-list">
          <li v-for="game in selectedGames" :key="game.id">
            <BaseImage
              :src="game.imageUrl"
              :alt="`${game.title} cover`"
              height="48px"
              width="48px"
              rounded="md"
            />
            <span>{{ game.title }}</span>
            <BaseButton
              variant="text"
              icon="mdi-close"
              :aria-label="`Remove ${game.title}`"
              @click="emit('toggle', game)"
            />
          </li>
        </ul>

        <p v-else-if="!surpriseMe" class="game-architect-selection-summary__empty">
          No games selected yet.
        </p>

        <p class="game-architect-selection-summary__count">
          {{ selectedGames.length }}<span v-if="!scaleMode"> / {{ maxSelected }}</span>
          selected
        </p>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, type PropType } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import type { GameArchitectGame } from '~/services/gameArchitectService'

const props = defineProps({
  games: {
    type: Array as PropType<GameArchitectGame[]>,
    default: () => []
  },
  selectedGames: {
    type: Array as PropType<GameArchitectGame[]>,
    default: () => []
  },
  scaleMode: {
    type: Boolean,
    default: false
  },
  surpriseMe: {
    type: Boolean,
    default: false
  },
  maxSelected: {
    type: Number,
    default: 3
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits<{
  toggle: [game: GameArchitectGame]
  surprise: []
}>()

const search = ref('')

const filteredGames = computed(() => {
  const query = search.value.trim().toLowerCase()
  if (!query) return props.games

  return props.games.filter(game =>
    game.title.toLowerCase().includes(query) ||
    game.genres.some(genre => genre.toLowerCase().includes(query))
  )
})

const emptyMessage = computed(() =>
  search.value.trim()
    ? 'Try another title or genre.'
    : 'Add games to your profile before using the Game Architect.'
)

const isSelected = (gameId: string) =>
  props.selectedGames.some(game => game.id === gameId)

const selectionDisabled = (gameId: string) =>
  !props.scaleMode &&
  !isSelected(gameId) &&
  props.selectedGames.length >= props.maxSelected
</script>

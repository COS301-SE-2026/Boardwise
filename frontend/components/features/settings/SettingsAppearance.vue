<template>
  <BaseCard class="pa-6">
    <header class="settings-section-header">
      <div>
        <h2 class="text-h5 font-weight-bold mb-1">
          Appearance & View
        </h2>

        <p class="text-body-2 text-medium-emphasis mb-0">
          Choose how Boardwise looks and how content is displayed.
        </p>
      </div>
    </header>

    <v-divider class="my-6" />

    <section
      class="settings-option-section"
      aria-labelledby="theme-heading"
    >
      <div class="mb-4">
        <h3
          id="theme-heading"
          class="text-subtitle-1 font-weight-bold"
        >
          Theme
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-1 mb-0">
          Choose the colour mode that is most comfortable for you.
        </p>
      </div>

      <div class="settings-choice-grid">
        <SettingsChoiceCard
          title="Light"
          description="Bright Boardwise theme"
          icon="mdi-white-balance-sunny"
          :selected="draft.theme === 'light'"
          @select="draft.theme = 'light'"
        />

        <SettingsChoiceCard
          title="Dark"
          description="Reduced-light interface"
          icon="mdi-weather-night"
          :selected="draft.theme === 'dark'"
          @select="draft.theme = 'dark'"
        />

        <SettingsChoiceCard
          title="System"
          description="Follow your device setting"
          icon="mdi-monitor"
          :selected="draft.theme === 'system'"
          @select="draft.theme = 'system'"
        />
      </div>
    </section>

    <v-divider class="my-6" />

    <section
      class="settings-option-section"
      aria-labelledby="density-heading"
    >
      <div class="mb-4">
        <h3
          id="density-heading"
          class="text-subtitle-1 font-weight-bold"
        >
          Content density
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-1 mb-0">
          Control how much information appears on screen at once.
        </p>
      </div>

      <div class="settings-choice-grid settings-choice-grid--two">
        <SettingsChoiceCard
          title="Comfortable"
          description="More spacing between content"
          icon="mdi-view-agenda-outline"
          :selected="draft.density === 'comfortable'"
          @select="draft.density = 'comfortable'"
        />

        <SettingsChoiceCard
          title="Compact"
          description="Fit more content on screen"
          icon="mdi-view-headline"
          :selected="draft.density === 'compact'"
          @select="draft.density = 'compact'"
        />
      </div>
    </section>

    <v-divider class="my-6" />

    <section
      class="settings-option-section"
      aria-labelledby="game-view-heading"
    >
      <div class="mb-4">
        <h3
          id="game-view-heading"
          class="text-subtitle-1 font-weight-bold"
        >
          Game display
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-1 mb-0">
          Choose how games appear in browsing areas.
        </p>
      </div>

      <div class="settings-choice-grid settings-choice-grid--two">
        <SettingsChoiceCard
          title="Grid"
          description="Visual cards with game artwork"
          icon="mdi-view-grid-outline"
          :selected="draft.gameView === 'grid'"
          @select="draft.gameView = 'grid'"
        />

        <SettingsChoiceCard
          title="List"
          description="Compact rows with more details"
          icon="mdi-view-list-outline"
          :selected="draft.gameView === 'list'"
          @select="draft.gameView = 'list'"
        />
      </div>
    </section>

    <v-divider class="my-6" />

    <section
      aria-labelledby="motion-heading"
      class="settings-motion-row"
    >
      <div>
        <h3
          id="motion-heading"
          class="text-subtitle-1 font-weight-bold"
        >
          Reduce motion
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-1 mb-0">
          Reduce non-essential animations and movement.
        </p>
      </div>

      <v-switch
        v-model="draft.reduceMotion"
        color="primary"
        hide-details
        aria-labelledby="motion-heading"
      />
    </section>

    <v-divider class="my-6" />

    <div class="settings-preview">
      <p class="text-subtitle-2 font-weight-bold mb-3">
        Preview
      </p>

      <BaseCard class="settings-preview__card pa-4">
        <div class="d-flex align-center ga-4">
          <div
            class="settings-preview__cover"
            aria-hidden="true"
          >
            <v-icon
              icon="mdi-dice-multiple"
              size="28"
            />
          </div>

          <div>
            <p class="font-weight-bold mb-1">
              Your Boardwise library
            </p>

            <p class="text-body-2 text-medium-emphasis mb-0">
              Games, rulebooks and resources in one place.
            </p>
          </div>
        </div>
      </BaseCard>
    </div>

    <div class="settings-actions mt-6">
      <BaseButton
        variant="secondary"
        :disabled="!hasChanges"
        @click="resetDraft"
      >
        Cancel
      </BaseButton>

      <BaseButton
        :disabled="!hasChanges"
        @click="save"
      >
        Save Changes
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

import SettingsChoiceCard from './SettingsChoiceCard.vue'

import {
  useAppearancePreferences,
  type BoardwiseTheme,
  type ContentDensity,
  type GameView
} from '~/composables/useAppearancePreferences'

const emit = defineEmits(['save'])

const {
  preferences,
  loadPreferences,
  savePreferences
} = useAppearancePreferences()

const draft = reactive<{
  theme: BoardwiseTheme
  density: ContentDensity
  gameView: GameView
  reduceMotion: boolean
}>({
  theme: 'system',
  density: 'comfortable',
  gameView: 'grid',
  reduceMotion: false
})

const copySavedPreferences = () => {
  draft.theme = preferences.value.theme
  draft.density = preferences.value.density
  draft.gameView = preferences.value.gameView
  draft.reduceMotion = preferences.value.reduceMotion
}

const hasChanges = computed(() => {
  return (
    draft.theme !== preferences.value.theme ||
    draft.density !== preferences.value.density ||
    draft.gameView !== preferences.value.gameView ||
    draft.reduceMotion !== preferences.value.reduceMotion
  )
})

const resetDraft = () => {
  copySavedPreferences()
}

const save = () => {
  const nextPreferences = {
    theme: draft.theme,
    density: draft.density,
    gameView: draft.gameView,
    reduceMotion: draft.reduceMotion
  }

  savePreferences(nextPreferences)

  emit('save', nextPreferences)
}

onMounted(() => {
  loadPreferences()
  copySavedPreferences()
})
</script>
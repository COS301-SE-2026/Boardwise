<template>
  <PageContainer>
    <Navbar />

    <header class="game-architect-status-header">

      <h1>Building your game</h1>

      <p>
        You may leave this page while Boardwise finishes the work.
      </p>
    </header>

    <GameArchitectJobProgress
      @complete="showCompletedResult"
    />
  </PageContainer>
</template>

<script setup >
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import GameArchitectJobProgress from
  '~/components/features/game-architect/GameArchitectJobProgress.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'

definePageMeta({
  middleware: 'auth'
})

const showCompletedResult = async () => {
  if (import.meta.client) {
    localStorage.setItem(
      'boardwise:game-architect-job',
      'completed'
    )
  }

  await navigateTo('/game-architect/result')
}
</script>
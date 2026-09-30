<template>
  <PageContainer>
    <Navbar />

    <header class="game-architect-status-header">

      <h1>Your game is ready</h1>

      <p>
        Review the generated concept and continue developing it.
      </p>
    </header>

    <GameArchitectResult
      @restart="startAnother"
      @view="viewGame"
    />
  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import GameArchitectResult from
  '~/components/features/game-architect/GameArchitectResult.vue'

definePageMeta({
  middleware: 'auth'
})

const startAnother = async () => {
  if (import.meta.client) {
    localStorage.removeItem('boardwise:game-architect-job')
  }

  await navigateTo('/game-architect')
}

const viewGame = () => {
  navigateTo('/library')
}
</script>
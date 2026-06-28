<template>
  <div class="collection-grid">
    <div class="grid-header">
      <h2>
        <v-icon left>mdi-grid</v-icon>
        Collection
      </h2>
      <span class="game-count">{{ games.length }} games</span>
    </div>

    <BaseGrid cols="260px" gap="24px">
      <BaseCard 
        v-for="game in games" 
        :key="game.id" 
        class="game-card"
        @click="viewGame(game.id)"
      >
        <div class="game-image-wrapper">
          <v-img
            :src="game.image || '/images/game-default.jpg'"
            height="260"
            cover
            class="game-image"
          />
          <BaseBadge v-if="game.isEssential" variant="primary" class="game-badge">
            Essential
          </BaseBadge>
        </div>
        <div class="game-info">
          <h3>{{ game.title || game.name }}</h3>
          <div class="game-tags">
            <span v-for="tag in game.tags || [game.category]" :key="tag" class="game-tag">
              {{ tag }}
            </span>
          </div>
        </div>
      </BaseCard>

      <BaseCard class="add-game-tile" @click="$emit('add-game')">
        <div class="add-game-icon">
          <v-icon size="36" color="var(--color-text-muted)">mdi-plus</v-icon>
        </div>
        <span class="add-game-title">Add to Collection</span>
        <p class="add-game-subtitle">Suggest a game to add to the vault</p>
      </BaseCard>
    </BaseGrid>
  </div>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

defineProps({
  games: {
    type: Array,
    required: true
  }
})

defineEmits(['add-game'])

const viewGame = (gameId) => {
  console.log('View game:', gameId)
}
</script>

<style scoped>
.collection-grid {
  padding: var(--space-2) 0;
}

.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.grid-header h2 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--fs-h3);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  margin: 0;
}

.game-count {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.game-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.game-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.game-image-wrapper {
  position: relative;
}

.game-image {
  background: var(--color-surface-alt);
}

.game-badge {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
}

.game-info {
  padding: var(--space-3);
}

.game-info h3 {
  margin: 0 0 var(--space-1) 0;
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.game-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.game-tag {
  font-size: var(--fs-small);
  font-weight: var(--fw-medium);
  padding: 2px var(--space-2);
  border-radius: var(--radius-pill);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  color: var(--color-text-muted);
}

.add-game-tile {
  border: 2px dashed var(--color-border-strong);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  padding: var(--space-6);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-base);
}

.add-game-tile:hover {
  border-color: var(--color-primary);
  background: rgba(109, 0, 55, 0.04);
}

.add-game-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-3);
  transition: all var(--transition-base);
}

.add-game-tile:hover .add-game-icon {
  background: var(--color-primary);
}

.add-game-tile:hover .add-game-icon .v-icon {
  color: var(--color-text-inverse) !important;
}

.add-game-title {
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-text-muted);
  transition: color var(--transition-base);
}

.add-game-tile:hover .add-game-title {
  color: var(--color-primary);
}

.add-game-subtitle {
  margin-top: var(--space-1);
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  opacity: 0.6;
}
</style>
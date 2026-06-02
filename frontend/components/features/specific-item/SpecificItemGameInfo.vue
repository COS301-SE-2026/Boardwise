<template>
  <BaseCard v-if="rulebook">
    <div class="game-info">

      <BaseFilterGroup title="Game details">
        <div class="stats">

          <div class="stat">
            <span class="stat__label">Players</span>
            <span class="stat__value">{{ rulebook.players }}</span>
          </div>

          <div class="stat">
            <span class="stat__label">Duration</span>
            <span class="stat__value">{{ rulebook.duration }}</span>
          </div>

          <div class="stat">
            <span class="stat__label">Age</span>
            <span class="stat__value">{{ rulebook.age }}</span>
          </div>

          <div class="stat">
            <span class="stat__label">Difficulty</span>
            <BaseBadge>{{ rulebook.difficulty }}</BaseBadge>
          </div>

          <div class="stat">
            <span class="stat__label">Category</span>
            <span class="stat__value">{{ rulebook.category }}</span>
          </div>

        </div>
      </BaseFilterGroup>

    </div>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'

defineProps({
  rulebook: { type: Object, default: null }
})
</script>

<style scoped>
.game-info {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: var(--space-5) var(--space-6);
  flex-wrap: wrap;
  gap: var(--space-4);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--space-4);
  padding: var(--space-4) 0;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat__label {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: var(--fw-medium);
}

.stat__value {
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

</style>
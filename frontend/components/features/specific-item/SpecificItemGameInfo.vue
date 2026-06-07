<template>
  <BaseCard v-if="rulebook">
    <div class="game-info">

      <BaseFilterGroup title="Game details">
        <div class="stats">

          <div v-for="stat in stats" :key="stat.label" class="stat">
            <span class="stat__label">{{ stat.label }}</span>
            <BaseBadge v-if="stat.badge">{{ stat.value }}</BaseBadge>
            <span v-else class="stat__value">{{ stat.value }}</span>
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

const stats = computed(() => [
  { label: 'Players',    value: props.rulebook?.players,    badge: false },
  { label: 'Duration',   value: props.rulebook?.duration,   badge: false },
  { label: 'Age',        value: props.rulebook?.age,        badge: false },
  { label: 'Difficulty', value: props.rulebook?.difficulty, badge: true  },
  { label: 'Category',   value: props.rulebook?.category,   badge: false },
])
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
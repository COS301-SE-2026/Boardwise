<template>
  <div class="stats-row">
    <v-card class="stat-card" flat @click="undefined">
      <div class="stat-icon"><v-icon size="20">mdi-dice-multiple</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{  games }}</span>
        <span class="stat-label">Games Owned</span>
      </div>
    </v-card>

    <button class="stat-card stat-card__clickable" @click="$emit('open')">
      <div class="stat-icon"><v-icon size="20">mdi-account-multiple</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{ friends }}</span>
        <span class="stat-label">Friends</span>
      </div>
    </button>

    <v-card class="stat-card" flat @click="undefined">
      <div class="stat-icon"><v-icon size="20">mdi-shield-account</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{ communities }}</span>
        <span class="stat-label">Communities</span>
      </div>
    </v-card>
  </div>
</template>

<script setup>

defineProps({
  games: { type: Number, default: 0 },
  friends: { type: Number, default: 0 },
  communities: { type: Number, default: 0 }
})

defineEmits(['open'])
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-inline: var(--space-12);
  margin-top: 25px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: var(--color-surface) !important;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm) !important;
  text-align: left;
}

.stat-card__clickable {
  cursor: pointer;
  font: inherit;
}

.stat-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bw-gold-muted, #f3e6c8);
  color: var(--color-primary);
}

.stat-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--fs-h3);
  font-weight: var(--fw-regular);
  color: var(--color-secondary);
  line-height: var(--lh-tight);
}

.stat-label {
  font-family: var(--font-body);
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

@media (max-width: 600px) {
  .stats-row {
    margin-inline: var(--space-4);
    grid-template-columns: 1fr;
  }
}
</style>
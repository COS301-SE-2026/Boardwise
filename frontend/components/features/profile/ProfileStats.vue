<template>
  <v-card class="status-header pa-3">
    <div class="status-header_stats">

      <div class="status-header_stat">
        <span class="status-header_value">{{ games }}</span>
        <span class="status-header_label">Games</span>
      </div>

      <v-divider vertical class="status-header_divider d-none d-sm-flex" />

      <button @click="$emit('open')" class="status-header_stat">
        <span class="status-header_value">{{ friends }}</span>
        <span class="status-header_label">Friends</span>
      </button>

      <v-divider vertical class="status-header_divider d-none d-sm-flex" />

      <div class="status-header_stat">
        <span class="status-header_value">{{ communities }}</span>
        <span class="status-header_label">Communities</span>
      </div>

    </div>
  </v-card>
</template>

<script setup>

defineProps({
  games: { type: Number, default: 0 },
  friends: { type: Number, default: 0 },
  communities: { type: Number, default: 0 },
  reviews: { type: Number, default: 0 },
})

defineEmits(['open'])
</script>

<style scoped>
.status-header {
  border-radius:  var(--radius-lg) !important;
  margin-inline:  var(--space-12);
  margin-top: -32px;
  max-height: fit-content;
  min-height: auto;
}

.status-header_stats {
  display:         flex;
  justify-content: space-around;
  align-items:     center;
  flex-wrap:       wrap;
  gap:             var(--space-6);
}

.status-header_stat {
  display:        flex;
  flex-direction: column;
  align-items:    center;
  gap:            var(--space-2);
  flex:           1;
}

.status-header_value {
  font-family: var(--font-display);
  font-size:   var(--fs-h1);
  font-weight: var(--fw-regular);
  color:       var(--color-primary);
  line-height: var(--lh-tight);
}

.status-header_label {
  font-family: var(--font-body);
  font-size:   var(--fs-small);
  color:       var(--color-text-muted);
}

.status-header_divider {
  height:      60px;
  align-self:  center;
}

button {
  border: none;
  background-color: var(--color-alabaster);
}
</style>
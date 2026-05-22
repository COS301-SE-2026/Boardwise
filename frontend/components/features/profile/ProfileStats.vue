<template>
  <v-card class="status-header pa-3">
    <div class="status-header__stats">

      <div class="status-header__stat">
        <span class="status-header__value">{{ games }}</span>
        <span class="status-header__label">Games</span>
      </div>

      <v-divider vertical class="status-header__divider d-none d-sm-flex" />

      <div class="status-header__stat">
        <span class="status-header__value">{{ friends }}</span>
        <span class="status-header__label">Friends</span>
      </div>

      <v-divider vertical class="status-header__divider d-none d-sm-flex" />

      <div class="status-header__stat">
        <span class="status-header__value">{{ communities }}</span>
        <span class="status-header__label">Communities</span>
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
</script>

<style scoped>
.status-header {
  border-radius:  var(--radius-lg) !important;
  margin-inline:  var(--space-12);
  margin-top: -50px;
  max-height: fit-content;
  min-height: auto;
}

.status-header__stats {
  display:         flex;
  justify-content: space-around;
  align-items:     center;
  flex-wrap:       wrap;
  gap:             var(--space-6);
}

.status-header__stat {
  display:        flex;
  flex-direction: column;
  align-items:    center;
  gap:            var(--space-2);
  flex:           1;
}

.status-header__value {
  font-family: var(--font-display);
  font-size:   var(--fs-h1);
  font-weight: var(--fw-regular);
  color:       var(--color-primary);
  line-height: var(--lh-tight);
}

.status-header__label {
  font-family: var(--font-body);
  font-size:   var(--fs-small);
  color:       var(--color-text-muted);
}

.status-header__divider {
  height:      60px;
  align-self:  center;
}
</style>
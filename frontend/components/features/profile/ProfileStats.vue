<template>
  <section class="stats-row">

    <BaseCard flush class="stat-card">
      <div class="stat-icon">
        <v-icon size="20">mdi-dice-multiple</v-icon>
      </div>

      <div class="stat-body">
        <span class="stat-value">{{  games }}</span>
        <span class="stat-label">Games Owned</span>
      </div>
    </BaseCard>

    <!-- Friends -->
    <button type="button" class="stat-card stat-card--button" @click="$emit('open')">
      <div class="stat-icon">
        <v-icon size="20">mdi-account-multiple</v-icon>
      </div>

      <div class="stat-body">
        <span class="stat-value">{{ friends }}</span>
        <span class="stat-label">Friends</span>
        <span v-if="friendsDelta" class="stat-delta">{{ friendsDelta }}</span>
      </div>
    </button>

    <BaseCard flush class="stat-card">
      <div class="stat-icon">
        <v-icon size="20">mdi-shield-account</v-icon>
      </div>

      <div class="stat-body">
        <span class="stat-value">{{ communities }}</span>
        <span class="stat-label">Communities</span>
      </div>
    </BaseCard>
  </section>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';

defineProps({
  games: { type: Number, default: 0 },
  friends: { type: Number, default: 0 },
  communities: { type: Number, default: 0 },
  gamesDelta: { type: String, default: '' },
  friendsDelta: { type: String, default: '' },
  communitiesDelta: { type: String, default: '' }
})

defineEmits(['open'])
</script>

<style scoped> 
.stat-card--button { 
  width: 100%; 
  border: none; 
  font: inherit; 
  text-align: left; 
  cursor: pointer; 
} 
</style>
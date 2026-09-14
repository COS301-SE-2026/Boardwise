<template>
  <div class="stats-row">
    <BaseCard flush class="stat-card">
      <div class="stat-icon"><v-icon size="20">mdi-dice-multiple</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{  games }}</span>
        <span class="stat-label">Games Owned</span>
      </div>
    </BaseCard>

    <button class="stat-card stat-card__clickable" @click="$emit('open')">
      <div class="stat-icon"><v-icon size="20">mdi-account-multiple</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{ friends }}</span>
        <span class="stat-label">Friends</span>
      </div>
    </button>

    <BaseCard flush class="stat-card">
      <div class="stat-icon"><v-icon size="20">mdi-shield-account</v-icon></div>
      <div class="stat-body">
        <span class="stat-value">{{ communities }}</span>
        <span class="stat-label">Communities</span>
      </div>
    </BaseCard>
  </div>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';

defineProps({
  games: { type: Number, default: 0 },
  friends: { type: Number, default: 0 },
  communities: { type: Number, default: 0 }
})

defineEmits(['open'])
</script>
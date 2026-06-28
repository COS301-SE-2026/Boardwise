<template>
  <BaseCard class="member-card">
    <div class="member-avatar">
      <v-avatar size="64">
        <v-img :src="member.avatar || '/images/avatar.jpg'" />
      </v-avatar>
    </div>
    <div class="member-info">
      <h3>{{ member.name }}</h3>
      <BaseBadge :variant="member.role.toLowerCase()">
        {{ member.role }}
      </BaseBadge>
    </div>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

defineProps({
  member: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.member-card {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-2);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.member-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.member-avatar {
  margin-bottom: var(--space-1);
}

.member-info h3 {
  margin: 0 0 var(--space-1) 0;
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
</style>
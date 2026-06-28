<template>
  <BaseCard class="community-card">
    <div class="card-image">
      <v-img
        :src="community.image || '/images/community-default.jpg'"
        height="160"
        cover
        class="community-image"
      />
      <BaseBadge :variant="community.type.toLowerCase()" class="card-badge">
        {{ community.type }}
      </BaseBadge>
    </div>

    <div class="card-content">
      <h3 class="community-name">{{ community.name }}</h3>
      <p class="community-description">{{ community.description }}</p>
      
      <div class="community-meta">
        <div class="member-avatars">
          <v-avatar 
            v-for="(member, index) in community.members_list?.slice(0, 3) || []" 
            :key="index"
            size="28"
            class="member-avatar"
          >
            <v-img :src="member.avatar || '/images/avatar.jpg'" />
          </v-avatar>
          <span v-if="community.members > 3" class="member-more">
            +{{ community.members - 3 }}
          </span>
        </div>
        <span class="member-count">{{ community.members }} members</span>
      </div>
    </div>

    <div class="card-actions">
      <BaseButton 
        variant="primary" 
        block
        @click.stop="$emit('click')"
      >
        View
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  community: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  community: {
    type: Object,
    required: true
  }
})

defineEmits(['click', 'join'])
</script>

<style scoped>
.community-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.community-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-image {
  position: relative;
  overflow: hidden;
}

.community-image {
  transition: transform 0.3s ease;
}

.community-card:hover .community-image {
  transform: scale(1.05);
}

.card-badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
}

.card-content {
  padding: var(--space-4);
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.community-name {
  margin: 0;
  font-size: var(--fs-h3);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.community-description {
  margin: 0;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  line-height: var(--lh-normal);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.community-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.member-avatars {
  display: flex;
  align-items: center;
}

.member-avatar {
  border: 2px solid var(--color-surface);
  margin-right: -8px;
}

.member-avatar:last-child {
  margin-right: 0;
}

.member-more {
  font-size: var(--fs-small);
  font-weight: var(--fw-medium);
  color: var(--color-text-muted);
  margin-left: var(--space-1);
}

.member-count {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  margin-left: auto;
}

.card-actions {
  padding: var(--space-3) var(--space-4) var(--space-4);
  border-top: 1px solid var(--color-border);
}

@media (max-width: 600px) {
  .community-card {
    max-width: 100%;
  }
}
</style>
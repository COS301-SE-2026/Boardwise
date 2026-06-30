<template>
  <div class="community-card" @click="$emit('click')">
    <div class="card-image">
      <v-img
        :src="community.image || '/images/community-default.jpg'"
        height="140"
        cover
        class="community-image"
      />
      <span class="card-badge" :class="community.type?.toLowerCase() || 'public'">
        {{ community.type || 'Public' }}
      </span>
    </div>

    <div class="card-content">
      <h3 class="community-name">{{ community.name }}</h3>
      <p class="community-description">{{ community.description }}</p>
      
      <div class="community-meta">
        <div class="member-info">
          <v-icon size="16" color="var(--color-text-muted)">mdi-account-group</v-icon>
          <span class="member-count">{{ community.members }} members</span>
        </div>
      </div>
    </div>

    <div class="card-actions">
      <button class="btn btn--primary btn--block" @click.stop="$emit('click')">
        View
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  community: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<style scoped>

.community-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.community-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.card-image {
  position: relative;
  overflow: hidden;
  background: var(--color-surface-alt);
}

.community-image {
  transition: transform var(--transition-base);
}

.community-card:hover .community-image {
  transform: scale(1.05);
}

.card-badge {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  font-size: var(--fs-small);
  font-weight: var(--fw-medium);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-pill);
  background: var(--bw-gold-muted);
  color: var(--bw-navy-ink);
}

.card-badge.public {
  background: var(--bw-gold-muted);
  color: var(--bw-navy-ink);
}

.card-badge.private {
  background: var(--bw-navy);
  color: var(--color-text-inverse);
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
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  font-family: var(--font-display);
}

.community-description {
  margin: 0;
  font-size: var(--fs-body);
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
  margin-top: var(--space-2);
}

.member-info {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.member-count {
  font-weight: var(--fw-medium);
}

.card-actions {
  padding: var(--space-3) var(--space-4) var(--space-4);
  border-top: 1px solid var(--color-border);
}

.btn--block {
  width: 100%;
  justify-content: center;
}
</style>
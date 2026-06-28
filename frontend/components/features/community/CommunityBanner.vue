<template>
  <BaseCard class="community-banner">
    <div class="banner-wrapper">
      <div class="banner-image-wrapper">
        <v-img
          :src="community.image || '/images/community-default.jpg'"
          height="200"
          cover
          class="banner-image"
        />
        <div class="banner-overlay"></div>
      </div>

      <div class="banner-content">
        <div class="banner-left">
          <div class="community-avatar">
            <v-img
              :src="community.image || '/images/community-default.jpg'"
              cover
              class="avatar-image"
            />
          </div>

          <div class="community-info">
            <div class="community-name-row">
              <h1>{{ community.name }}</h1>
              <BaseBadge :variant="community.type.toLowerCase()">
                {{ community.type }}
              </BaseBadge>
            </div>
            <p class="community-description">{{ community.description }}</p>
            <div class="community-members">
              <div class="member-avatars">
                <v-avatar 
                  v-for="(member, index) in community.members_list?.slice(0, 3)" 
                  :key="index"
                  size="28"
                  class="member-avatar"
                >
                  <v-img :src="member.avatar || '/images/avatar.jpg'" />
                </v-avatar>
              </div>
              <span class="member-count">{{ community.members }} members</span>
            </div>
          </div>
        </div>

        <div class="banner-actions">
          <BaseButton variant="secondary" size="md" class="action-btn" @click="$emit('edit')">
            <v-icon>mdi-pencil</v-icon>
          </BaseButton>
          <BaseButton 
            :variant="isJoined ? 'secondary' : 'primary'" 
            size="lg"
            @click="$emit('toggle-join')"
          >
            <v-icon v-if="isJoined" left size="18">mdi-check</v-icon>
            {{ isJoined ? 'Joined' : 'Join Community' }}
          </BaseButton>
        </div>
      </div>
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
  },
  isJoined: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-join', 'edit'])
</script>

<style scoped>
.community-banner {
  padding: 0;
  overflow: hidden;
}

.banner-wrapper {
  position: relative;
}

.banner-image-wrapper {
  position: relative;
  overflow: hidden;
}

.banner-image {
  transition: transform 0.7s ease;
}

.banner-wrapper:hover .banner-image {
  transform: scale(1.03);
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6) 0%, transparent 40%);
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--space-6);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex: 1;
  min-width: 0;
}

.community-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  border: 3px solid var(--color-surface);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.community-info {
  color: var(--color-text-inverse);
  min-width: 0;
}

.community-name-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
  flex-wrap: wrap;
}

.community-name-row h1 {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-text-inverse);
}

.community-description {
  margin: 0 0 var(--space-1) 0;
  font-size: var(--fs-small);
  opacity: 0.9;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.community-members {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.member-avatars {
  display: flex;
}

.member-avatar {
  border: 2px solid var(--color-text-inverse);
  margin-right: -6px;
}

.member-avatar:last-child {
  margin-right: 0;
}

.member-count {
  font-size: var(--fs-small);
  font-weight: var(--fw-medium);
}

.banner-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-shrink: 0;
}

.action-btn {
  width: 44px;
  height: 44px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text-inverse);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-4);
  }

  .banner-left {
    width: 100%;
  }

  .community-avatar {
    width: 56px;
    height: 56px;
  }

  .community-name-row h1 {
    font-size: var(--fs-h3);
  }

  .community-description {
    max-width: 100%;
    white-space: normal;
  }

  .banner-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
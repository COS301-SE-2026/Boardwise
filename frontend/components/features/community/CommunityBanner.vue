<template>
  <BaseCard class="banner">
    <div class="banner-top">

      <div class="left">
        <div class="banner-image">
          <img :src="community.image" :alt="community.name" />
        </div>

        <v-card-text class="info">
          <h1>{{ community.name }}</h1>
          <v-chip size="small">{{ community.type }}</v-chip>
          <p class="description">{{ community.description }}</p>
          <p class="members">{{ community.members }} members</p>
        </v-card-text>
      </div>

      <v-card-actions>
        <BaseButton @click="showEdit = true">Edit</BaseButton>
        <BaseButton @click="joined = !joined">
          {{ joined ? 'Leave' : 'Join Community' }}
        </BaseButton>
      </v-card-actions>

    </div>

    <CommunityEditModal
      v-model="showEdit"
      :community="community"
      @save="handleSave"
    />
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import CommunityEditModal from './CommunityEditModal.vue'

defineProps({
  community: { type: Object, required: true }
})

const joined = ref(false)

const showEdit = ref(false)
 
const handleSave = (data) => {
  console.log('Save community:', data)
}
</script>

<style scoped>
.banner {
  width: 100%;
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  background: #F9F4E3;
}

.banner-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  gap: var(--space-6);
  align-items: center;
}

.banner-image {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.banner-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  align-items: flex-start;
}

.info h1 {
  margin: 0;
  font-size: var(--fs-h1);
  line-height: var(--lh-tight);
}

.description {
  color: var(--color-text-muted);
  font-size: var(--fs-body);
  margin: 0;
}

.members {
  color: var(--color-text-muted);
  font-size: var(--fs-small);
  margin: 0;
}

@media (max-width: 768px) {
  .banner-top {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-6);
  }

  .left {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
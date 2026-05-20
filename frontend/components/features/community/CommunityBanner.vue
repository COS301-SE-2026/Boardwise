<template>
  <BaseCard class="banner">

    <div class="banner-top">

      <div class="left">

        <div class="banner-image">
          <img :src="community.image" :alt="community.name" />
        </div>

        <div class="info">
          <h1>{{ community.name }}</h1>
          <BaseBadge>{{ community.type }}</BaseBadge>
          <p class="description">{{ community.description }}</p>
          <p class="members">{{ community.members }} members</p>
        </div>

      </div>

      <div class="banner-actions">
        <BaseButton @click="showEdit = true">Edit</BaseButton>
        <BaseButton @click="joined = !joined">
          {{ joined ? 'Leave' : 'Join Community' }}
        </BaseButton>
    </div>
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
import BaseBadge from '~/components/ui/BaseBadge.vue'
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
  padding: 32px;
  border-radius: 24px;
  background: #F9F4E3;
}

.banner-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
 
.left {
  display: flex;
  gap: 24px;
  align-items: center;
}
 
.banner-image {
  width: 100px;
  height: 100px;
  border-radius: 16px;
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
  gap: 6px;
}
 
.info h1 {
  margin: 0;
  font-size: 2rem;
}
 
.description {
  color: #555;
  margin: 0;
}
 
.members {
  color: #999;
  font-size: 0.85rem;
  margin: 0;
}
 
@media (max-width: 768px) {
  .banner-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }
 
  .banner-actions {
  display: flex;
  gap: 12px;
}
  .left {
    flex-direction: column;
    align-items: flex-start;
  }

}
</style>
<template>
  <BaseCard class="community-card" @click="navigateToCommunity">

    <v-img
      :src="community.image"
      height="180"
      cover
    />

    <v-card-text class="card-body">
      <h3>{{ community.name }}</h3>
      <BaseBadge :variant="community.type">
        {{ community.type }}
      </BaseBadge>
      <p class="description">{{ community.description }}</p>
    </v-card-text>

    <v-card-actions class="card-actions">
      <BaseButton
        block
        @click.stop="router.push(`/community/${community.id}/chat`)"
      >
        View
      </BaseButton>
    </v-card-actions>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  community: { type: Object, required: true }
})

defineEmits(['join'])

const router = useRouter()

const navigateToCommunity = () => {
  router.push(`/community/${props.community.id}`)
}
</script>

<style scoped>
.community-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}

.community-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  flex: 1;
}

h3 {
  margin: 0;
  font-size: var(--fs-h4);
  color: var(--color-secondary);
}

.description {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  line-height: var(--lh-normal);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  padding: 0 var(--space-4) var(--space-4);
}
</style>
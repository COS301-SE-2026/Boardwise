<template>
  <BaseCard clickable @click="navigateToCommunity">
    <template #media>
      <BaseImage
        :src="community.imageUrl"
        :alt="community.name"
        height="200px"
      />

      <BaseBadge :variant="community.visibility">
        {{ community.visibility }}
      </BaseBadge>
    </template>

    <p class="card-title">
      {{ community.name }}
    </p>

    <p class="card-meta">
      {{ community.description }}
    </p>
  </BaseCard>
</template>

<script setup>
import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

const props = defineProps({
  community: { type: Object, required: true }
})

const router = useRouter()

const navigateToCommunity = () => {
  router.push(`/community/${props.community.id}`)
}
</script>

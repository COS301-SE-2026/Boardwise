<template>
  <BaseCard  >

    <BaseImage
      :src="community.image"
      :alt="community.name"
      height="180px"
      class="rounded-t-lg"
    />

    <div class="d-flex flex-column ga-3 pa-4 flex-grow-1">
      <h3 class="mb-0">{{ community.name }}</h3>

      <BaseTag>
        {{ community.type }}
      </BaseTag>

      <p class="description mb-0">{{ community.description }}</p>

      <BaseButton class="mt-auto"
        block
        @click.stop="navigateToCommunity"
      >
        View
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup>

import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseTag from '~/components/ui/BaseTag.vue'

const props = defineProps({
  community: { type: Object, required: true }
})

const router = useRouter()

const navigateToCommunity = () => {
  router.push(`/community/${props.community.id}`)
}
</script>

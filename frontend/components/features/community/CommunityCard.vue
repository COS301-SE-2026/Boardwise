<template>
  <BaseCard  >

    <div>
      <BaseImage
        :src="community.imageUrl"
        :alt="community.name"
        height="180px"
        class="rounded-t-lg"
      />

      <BaseBadge
        class="badge--absolute text-white"
        :variant="community.visibility"
      >
        {{ community.visibility }}
      </BaseBadge>
    </div>


    <div class="d-flex flex-column ga-3 pa-4 flex-grow-1">
      <h3 class="mb-0">{{ community.name }}</h3>

      <p class="text-body-2 text-medium-emphasis mb-0">
        {{ community.description }}
      </p>

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
import BaseBadge from '~/components/ui/BaseBadge.vue'

const props = defineProps({
  community: { type: Object, required: true }
})

const router = useRouter()

const navigateToCommunity = () => {
  router.push(`/community/${props.community.id}`)
}
</script>

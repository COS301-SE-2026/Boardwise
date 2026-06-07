<template>
  <NuxtLink :to="`/library/${rulebook.id}`" class="text-decoration-none text-inherit">
    <BaseCard class="cursor-pointer overflow-hidden pa-0 " style="width: 220px; flex-shrink: 0;">
      <BaseImage :src="rulebook.image" :alt="rulebook.title" height="280px" fit="cover" />
      <div class="pa-4">

        <p class="text-body-2 font-weight-bold mb-1">
          {{ rulebook.title }}
        </p>

        <p class="text-caption text-medium-emphasis mb-0">
          {{ rulebook.genre }}
        </p>

      </div>
    </BaseCard>
  </NuxtLink>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

defineProps({
  rulebook: Object
})
</script>
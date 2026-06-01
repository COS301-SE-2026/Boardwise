<template>
  <v-card rounded="xl" elevation="1">

    <v-img :src="rulebook?.image" :alt="rulebook?.title" height="280" cover />

    <div class="pa-10">

      <p class="text-caption text-uppercase font-weight-bold text-primary mb-2">
        {{ rulebook?.category }}
      </p>

      <h1 class="text-h4 font-weight-bold mb-4">{{ rulebook?.title }}</h1>

      <div class="d-flex flex-wrap gap-4 mb-2">
        <v-chip size="small" prepend-icon="mdi-account-group">{{ rulebook?.players }} players</v-chip>
        <v-chip size="small" prepend-icon="mdi-clock-outline">{{ rulebook?.duration }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-gauge">{{ rulebook?.difficulty }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-account">Age {{ rulebook?.age }}</v-chip>
      </div>

      <v-divider class="my-7" />

      <h2 class="text-h6 font-weight-bold mb-4">{{ page?.title }}</h2>

      <p class="text-body-1 text-medium-emphasis" style="line-height: 1.9;">
        {{ page?.content }}
      </p>

      <v-divider class="mt-10 mb-6" />

      <div class="d-flex justify-space-between">
        <BaseButton variant="secondary" :disabled="isFirst" @click="$emit('prev')">
          <v-icon start>mdi-arrow-left</v-icon>
          Previous
        </BaseButton>

        <BaseButton :disabled="isLast" @click="$emit('next')">
          Next
          <v-icon end>mdi-arrow-right</v-icon>
        </BaseButton>
      </div>

    </div>
  </v-card>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  rulebook: Object,
  page: Object,
  isFirst: Boolean,
  isLast: Boolean
})

defineEmits(['prev', 'next'])
</script>
<template>
  <v-card rounded="xl" elevation="1">
    <BaseImage :src="rulebook?.image" :alt="rulebook?.title" height="280px" fit="cover" />

    <div class="pa-10">
      <p class="text-caption text-uppercase font-weight-bold text-primary mb-2">
        {{ rulebook?.category }}
      </p>

      <h1 class="text-h4 font-weight-bold mb-4">{{ rulebook?.title }}</h1>

      <div class="d-flex flex-wrap ga-4 mb-2">
        <v-chip size="small" prepend-icon="mdi-account-group">{{ rulebook?.players }} players</v-chip>
        <v-chip size="small" prepend-icon="mdi-clock-outline">{{ rulebook?.duration }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-gauge">{{ rulebook?.difficulty }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-account">Age {{ rulebook?.age }}</v-chip>
      </div>

      <v-divider class="my-7" />

      <h2 class="text-h6 font-weight-bold mb-4">Section {{ (page?.index ?? 0) + 1 }}</h2>

      <p class="text-body-1 text-medium-emphasis" style="line-height: 1.9;" v-html="highlightedContent" />
        <!-- {{ page?.content }} -->

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
import { computed } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
  rulebook: Object,
  page: Object,
  isFirst: Boolean,
  isLast: Boolean, 
  searchQuery: {
    type: String,
    default: ''
  }
})

const highlightedContent = computed(() => {
  const text = props.page?.content ?? ''
  if (!props.searchQuery.trim()) return text
  
  const escaped = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex   = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<mark style="background: #fff176; border-radius: 2px;">$1</mark>')
})

defineEmits(['prev', 'next'])
</script>
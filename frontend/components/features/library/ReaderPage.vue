<template>
  <v-card rounded="xl" elevation="1">
    <v-img :src="rulebook?.coverUrl" :alt="rulebook?.title" height="280" cover />

    <div class="pa-10">
      <p class="text-caption text-uppercase font-weight-bold text-primary mb-2">
        {{ formattedGenres }}
      </p>

      <h1 class="text-h4 font-weight-bold mb-4">{{ rulebook?.title }}</h1>

      <div class="d-flex flex-wrap ga-4 mb-2">
        <v-chip size="small" prepend-icon="mdi-account-group">{{ formattedPlayerCount }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-clock-outline">{{ rulebook?.duration }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-account">Age {{ rulebook?.age }}</v-chip>
      </div>

      <v-divider class="my-7" />

      <h2 class="text-h6 font-weight-bold mb-4">Section {{ (page?.index ?? 0) + 1 }}</h2>

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
import {computed} from 'vue';

const props = defineProps({
  rulebook: Object,
  page: Object,
  isFirst: Boolean,
  isLast: Boolean
})

const formattedPlayerCount = computed(() => {
  if(!props.rulebook) return '0 players';

  const min = props.rulebook.minPlayers;
  const max = props.rulebook.maxPlayers;

  if(min == max){
    return `${min} players`;
  }
  return `${min} - ${max} players`;
});

const formattedGenres = computed(() => {
  if(!props.rulebook) return "";

  const genreArray = props.rulebook.genres;
  return `${genreArray.join(', ')}`;
})

defineEmits(['prev', 'next'])
</script>
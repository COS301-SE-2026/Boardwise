<template>
  <v-card rounded="xl" elevation="1">
    <BaseImage :src="rulebook?.coverUrl" :alt="rulebook?.title" height="280px" fit="cover" />

    <div class="pa-10">
      <p class="text-caption text-uppercase font-weight-bold text-primary mb-2">
        {{ formattedGenres }}
      </p>

      <h1 class="text-h4 font-weight-bold mb-4">{{ rulebook?.title }}</h1>

      <div class="d-flex flex-wrap ga-4 mb-2">
        <v-chip size="small" prepend-icon="mdi-account-group">{{ formattedPlayerCount }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-clock-outline">{{ rulebook?.duration }}</v-chip>
        <v-chip size="small" prepend-icon="mdi-account">{{ rulebook?.minAge }}</v-chip>
      </div>

      <v-divider class="my-7" />

      <h2 class="text-h6 font-weight-bold mb-4">Section {{ (page?.index ?? 0) + 1 }}</h2>

      <!-- Read view -->
      <template v-if="!isEditing">
        <p class="text-body-1 text-medium-emphasis" style="line-height: 1.9;">
          <template v-for="(segment, i) in contentSegments" :key="`${i}-${segment.text.length}`">
            <mark v-if="segment.active" class="search-highlight search-highlight--active">{{ segment.text }}</mark>
            <mark v-else-if="segment.highlight" class="search-highlight">{{ segment.text }}</mark>
            <span v-else>{{ segment.text }}</span>
          </template>
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
      </template>

      <!-- Edit view -->
      <template v-else>
        <BaseTextArea
          v-model="editContent"
          :rows="10"
          placeholder="Edit section content..."
        />

        <v-divider class="mt-6 mb-4" />

        <div class="d-flex justify-end ga-3">
          <BaseButton variant="secondary" :disabled="isSaving" @click="handleCancel">
            Cancel
          </BaseButton>
          <BaseButton :loading="isSaving" @click="handleSave">
            <v-icon start>mdi-content-save</v-icon>
            Save
          </BaseButton>
        </div>
      </template>
    </div>
  </v-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'

const props = defineProps({
  rulebook: Object,
  page: Object,
  isFirst: Boolean,
  isLast: Boolean, 
  isEditing:       { type: Boolean, default: false },
  isSaving:        { type: Boolean, default: false },
  searchQuery: {
    type: String,
    default: ''
  },

  activeOccurrence: {
    type: Number,
    default: -1
  }
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
  return props.rulebook?.genres?.join(', ') ?? '';
});

const contentSegments = computed(() => {
  const text = props.page?.content ?? ''
  if (!props.searchQuery.trim()) return [{ text, highlight: false, active: false }]

  const escaped = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex   = new RegExp(`(${escaped})`, 'gi')
  const parts   = text.split(regex)

  let matchCount = 0
  return parts.map(part => {
    const isMatch = new RegExp(`^${escaped}$`, 'i').test(part)
    const isActive = isMatch && matchCount === props.activeOccurrence
    if (isMatch) matchCount++
    return { text: part, highlight: isMatch, active: isActive }
  })
})

const emit = defineEmits(['prev', 'next', 'save', 'cancel'])

watch(() => props.isEditing, (val) => {
  if (val) editContent.value = props.page?.content ?? ''
})

// watch(() => props.page, (val) => {
//   if (props.isEditing) editContent.value = val?.content ?? ''
// })
watch(() => props.page?.content, (newContent) => {
  if(props.isEditing){
    editContent.value = newContent ?? '';
  }
});

const editContent = ref('')

const handleSave = () => {
  emit('save', editContent.value)
}

const handleCancel = () => {
  editContent.value = ''
  emit('cancel')
}

</script>

<style scoped>
.search-highlight {
  background: #fff176;
  border-radius: 2px;
  padding: 0 2px;
}

.search-highlight--active {
  background: #ffb300;
  outline: 2px solid #e65100;
}
</style>
<template>
  <BaseCard class="games-card"">

    <template #media>
      <BaseImage :src="image" :alt="title" height="200px" />
    </template>
 
    <div class="game-card__content">
      <p class="card-title">
        {{ decodedTitle }}
      </p>

      <p class="card-meta">
        {{ decodedCategory }}
      </p>

      <BaseButton
        variant="text"
        size="small"
        class="game-card__remove"
        @click.stop="openDelete = true"
      >
        <v-icon size="16">mdi-delete-outline</v-icon>
      </BaseButton>
    </div>

    <RemoveGameModal v-model="openDelete" @confirm="handleRemove" />
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import RemoveGameModal from './RemoveGameModal.vue'

import { computed, ref } from 'vue'

const props = defineProps({
  id: { type: String, required: true },
  title: { type: String, default: '' },
  category: { type: String, default: '' },
  image: { type: String, default: '' }
})

const emit = defineEmits(['remove'])

const openDelete = ref(false);

const decodedTitle = computed(() => decodeEntity(props.title));
const decodedCategory = computed(() => decodeEntity(props.category));

function handleRemove(){
  emit('remove');
}

function decodeEntity(entity) {
  if(!entity) return ''
  return entity.replaceAll(/&#39;/g, "'")
              .replaceAll(/&quot;/g, '"')
              .replaceAll(/&amp;/g, '&')
              .replaceAll(/&lt;/g, '<')
              .replaceAll(/&gt;/g, '>')
}
</script>
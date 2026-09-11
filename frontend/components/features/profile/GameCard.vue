<template>
  <BaseCard clickable @click="openDelete = true">

    <template #media>
      <BaseImage :src="image" alt="title" height="200px" />
    </template>
 
    <p class="card-title">
      {{ decodedTitle }}
    </p>

    <p class="card-meta">
      {{ decodedCategory }}
    </p>

    <RemoveGameModal v-if="removable" v-model="openDelete" @confirm="handleRemove()" ></RemoveGameModal>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import RemoveGameModal from './RemoveGameModal.vue'

import { computed, ref } from 'vue'

const props = defineProps({
  id: { type: String, required: true },
  title: String,
  category: String,
  image: String,
  removable: { type: Boolean, default: false }
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
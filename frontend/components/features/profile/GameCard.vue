<template>
  <BaseCard class="game-card" @click="openDelete = true">

    <v-img 
      :src="image" 
      :alt="title"
       height="240" 
       cover 
    />

    <div class="game-card__content">
      <h3 class="game-card__title">
        {{ decodedTitle }}
      </h3>

      <p class="game-card__category">
        {{ decodedCategory }}
      </p>
    </div>

    <RemoveGameModal v-model="openDelete" @confirm="handleRemove()" ></RemoveGameModal>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import RemoveGameModal from './RemoveGameModal.vue'

import { computed, ref } from 'vue'

const props = defineProps({
  id: { type: String, required: true },
  title: String,
  category: String,
  image: String,
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

<style scoped>

.game-card {
  display: flex;
  flex-direction: column;

  width: 100%;
  height: 320px;
  
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;

  transition:
    transform .2s ease,
    box-shadow .2s ease;
}

.game-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md) !important;
}

.game-card__title {
    font-family: var(--font-body);
    font-size: 1.15rem;
    font-weight: 600;
    color: var(--color-secondary);
    line-height: 1.3;

    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;

    overflow: hidden;
}

.game-card__content {
  padding: 16px;

  display: flex;
  flex-direction: column;
  gap: 8px;

  flex: 1;
}

.game-card__category {
    margin-top: .5rem;
    color: #777;
    font-size: .9rem;
}
</style>
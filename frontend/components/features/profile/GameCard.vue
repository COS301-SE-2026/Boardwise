<template>
  <BaseCard class="game-card" @click="openDelete = true">

    <v-img :src="image" :alt="title" height="180" cover />

    <v-card-text>
      <h3 class="game-card__title ma-0">{{ decodedTitle }}</h3>
      <p class="game-card__category ma-0 mt-1">{{ decodedCategory }}</p>
    </v-card-text>

    <!--TEMPORARY METHOD OF DELETING-->
    <RemoveGameModal v-model="openDelete" @confirm="handleRemove()" ></RemoveGameModal>
    <!-- <RulebookDetail
      v-model="showDetail"
      :game="{ title, category, image }"
    /> -->

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import RemoveGameModal from './RemoveGameModal.vue';
import { computed } from 'vue';
// import RulebookDetail from '~/components/features/library/RulebookDetail.vue'

const props = defineProps({
  id: { type: String, required: true },
  title: String,
  category: String,
  image: String,
})

const showDetail = ref(false)
const openDelete = ref(false);
const decodedTitle = computed(() => decodeEntity(props.title));
const decodedCategory = computed(() => decodeEntity(props.category));

async function handleRemove(){
  try{
    emit('remove');
  }
  catch{
    console.error('Failed to remove game', err)
  }
}

function decodeEntity(entity) {
  if(!entity) return ''
  return entity.replace(/&#39;/g, "'")
              .replace(/&quot;/g, '"')
              .replace(/&amp;/g, '&')
              .replace(/&lt;/g, '<')
              .replace(/&gt;/g, '>')
}

const emit = defineEmits(['remove'])

</script>

<style scoped>

.game-card {
  /* cursor:     pointer; */
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}
.game-card:hover {
  transform:  translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.game-card__title {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  font-weight: var(--fw-regular);
  color: var(--color-secondary);
}

.game-card__category {
  font-family: var(--font-body);
  font-size:   var(--fs-small);
  color: var(--color-text-muted);
}
</style>
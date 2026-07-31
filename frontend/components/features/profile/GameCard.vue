<template>
  <BaseCard class="game-card" @click="openDelete = true">

    <BaseImage 
      :src="image" 
      :alt="title" 
    />

    <v-card-text class="pa-4">
      <h3 class="card-title">
        {{ decodedTitle }}
      </h3>

      <p class="card-meta">
        {{ decodedCategory }}
      </p>
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
import BaseImage from '~/components/ui/BaseImage.vue';
import RemoveGameModal from './RemoveGameModal.vue';

import { computed, ref } from 'vue';
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
  return entity.replaceAll(/&#39;/g, "'")
              .replaceAll(/&quot;/g, '"')
              .replaceAll(/&amp;/g, '&')
              .replaceAll(/&lt;/g, '<')
              .replaceAll(/&gt;/g, '>')
}

const emit = defineEmits(['remove'])

</script>

<style scoped>

.game-card {
  cursor: pointer;
}
</style>
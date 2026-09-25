<template>
  <BaseCard
    clickable
    class="games-card"
    @click="handleBoardgameRedirect"
    >

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
import { useRouter } from 'vue-router'
import { useProfile } from '~/composables/useProfile'
import { useSnackBar } from '~/composables/useSnackbar'

const { fetchGetBoardgameRulebookId, error } = useProfile();
const { show } = useSnackBar();

const props = defineProps({
  id: { type: String, required: true },
  title: { type: String, default: '' },
  category: { type: String, default: '' },
  image: { type: String, default: '' }
})

const router = useRouter()

const emit = defineEmits(['remove'])

const openDelete = ref(false);

const decodedTitle = computed(() => decodeEntity(props.title));
const decodedCategory = computed(() => decodeEntity(props.category));

function handleRemove(){
  emit('remove');
}

const handleBoardgameRedirect = async () => {
  try{
    const rulebookId = await fetchGetBoardgameRulebookId(props.id);
    router.push(`/library/read/${rulebookId}`);
  }catch(err){
    show(error.value, 'error');
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
</script>
<template>
  <ReaderLayout
    ref="readerLayoutRef"
    v-if="currentRulebook"
    :rulebook="currentRulebook"
    :chunks="rulebookText?.chunks ?? []"
  />

  <div v-else-if="isLoading" class="d-flex justify-center align-center" style="height: 60vh;">
    <v-progress-circular indeterminate color="primary" />
  </div>

  <v-empty-state
    v-else
    title="Rulebook not found"
    icon="mdi-book-off-outline"
  >
    <template #actions>
      <BaseButton @click="router.push('/library')">
        ← Back to Library
      </BaseButton>
    </template>
  </v-empty-state>
</template>

<script setup>
import { useLibrary } from '~/composables/useLibrary'
import { useEditLock } from '~/composables/useEditLock'
import { useStomp } from '~/composables/useStomp'

import ReaderLayout from '~/components/features/library/ReaderLayout.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { ref, onMounted } from 'vue';

const readerLayoutRef = ref(null);
const route = useRoute()
const router = useRouter()

const {
  currentRulebook,
  rulebookText,
  isLoading,
  getRulebookById,
  getRulebookText
} = useLibrary()

const {
    lockHeldBy,
    lockExpiresAt,
    currentVersion,
} = useEditLock()

const { connect } = useStomp()

onMounted(async () => {
  try{
    connect();
  } catch(err){
    console.warn('Websocket failed to connect', err)
  }

  await getRulebookById(route.params.id)
  await getRulebookText(route.params.id)
  
  if(rulebookText.value){
    currentVersion.value = rulebookText.value.version;
    lockHeldBy.value = rulebookText.value.lockHeldBy;
  }
  
  if(currentRulebook.value){
    lockExpiresAt.value = currentRulebook.value.lockExpiresAt;
  }
})
</script>
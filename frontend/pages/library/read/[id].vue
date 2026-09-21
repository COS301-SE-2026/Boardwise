<template>
  <PageContainer>

    <BaseLoadingState v-if="isLoading" />

    <ReaderLayout
      v-else-if="currentRulebook"
      ref="readerLayoutRef"
      :rulebook="currentRulebook"
      :chunks="rulebookText?.chunks ?? []"
    />

    <BaseEmptyState
      v-else
      title="Rulebook not found"
      message="This rulebook may have been removed or the link is incorrect."
    />
  </PageContainer>
</template>

<script setup>
import { useLibrary } from '~/composables/useLibrary'
import { useEditLock } from '~/composables/useEditLock'
import { useStomp } from '~/composables/useStomp'

import ReaderLayout from '~/components/features/library/ReaderLayout.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { ref, onMounted } from 'vue';
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

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
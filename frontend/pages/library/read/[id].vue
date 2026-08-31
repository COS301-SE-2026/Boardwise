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
import { useEditLock }     from '~/composables/useEditLock'
import { useReaderSocket } from '~/composables/useReaderSocket'

import ReaderLayout from '~/components/features/library/ReaderLayout.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { ref } from 'vue';

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
    isEditing,
    stopEditing
} = useEditLock()

// Websocket
const { connect: connectSocket } = useReaderSocket(
    String(route.params.id),
    {
      onLockAcquired: ({lockedByUsername, expiresAt, currentVersion: serverVersion}) => {
        lockHeldBy.value = lockedByUsername;
        lockExpiresAt.value = expiresAt;
        currentVersion.value = serverVersion;
      },
      onLockReleased: () => {
        lockHeldBy.value = null;
        lockExpiresAt.value = null;
      },
      onDeltaCommitted: (payload) => {
        currentVersion.value = payload.version;

        const chunk = rulebookText.value?.chunks.find(c => c.chunkId === payload.chunkId);
        
        if(chunk){
            chunk.content = payload.deltaContent;
        }else{
          console.warn("Could not find chunk locally to update:", payload.chunkId)
        }
      },
      onChunkInserted: ({chunkId, content, index, version}) => {
        currentVersion.value = version;
        rulebookText.value?.chunks.splice(index, 0, {chunkId, content, index});
      },
      onChunkDeleted: ({chunkId, version}) => {
        currentVersion.value = version;
        if(rulebookText.value?.chunks){
          // rulebookText.value.chunks = rulebookText.value.chunks.filter(c=> c.chunkId !== chunkId);
          const index = rulebookText.value.chunks.findIndex(c => c.chunkId === chunkId);
          if(index !== -1){
            rulebookText.value.chunks.splice(index, 1);
          }
        }
      },
      onReconnect: async () => {
        if(isEditing.value){
          show('Connection restored, but you may have missed updates. Save carefully.', 'warning');
          return;
        }

        if(readerLayoutRef.value){
          await readerLayoutRef.value.reconcileStaleState();
        }
      }
    }
)

onMounted(async () => {
  await getRulebookById(route.params.id)
  await getRulebookText(route.params.id)
  
  if(rulebookText.value){
    currentVersion.value = rulebookText.value.version;
    lockHeldBy.value = rulebookText.value.lockHeldBy;
  }
  
  if(currentRulebook.value){
    lockExpiresAt.value = currentRulebook.value.lockExpiresAt;
  }

  try {
    connectSocket()
  }catch(err) {
    console.warn('Websocket connection failed - lock events unavailable', err);
  }
})
</script>
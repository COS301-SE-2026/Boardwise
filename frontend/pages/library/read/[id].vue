<template>
  <ReaderLayout 
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
    isEditing,
    stopEditing
} = useEditLock()

// Websocket
// TODO: Double check
const { connect: connectSocket } = useReaderSocket(
    String(route.params.id),

    // Another user acquired the lock 
    ({ lockedBy, expiresAt }) => {
        lockHeldBy.value    = lockedBy
        lockExpiresAt.value = expiresAt
    },

    // Lock was released 
    () => {
        lockHeldBy.value    = null
        lockExpiresAt.value = null
    }
)

onMounted(async () => {
  await getRulebookById(route.params.id)
  await getRulebookText(route.params.id)
  await connectSocket()
})
</script>
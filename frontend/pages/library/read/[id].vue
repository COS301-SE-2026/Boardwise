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

onMounted(async () => {
  await getRulebookById(route.params.id)
  await getRulebookText(route.params.id)
})
</script>
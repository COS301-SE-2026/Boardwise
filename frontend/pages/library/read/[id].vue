<template>
  <ReaderLayout v-if="rulebook" :rulebook="rulebook" />

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
import { rulebooks } from '~/services/mockData/rulebooks.js'
import ReaderLayout from '~/components/features/library/ReaderLayout.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const route = useRoute()
const router = useRouter()

const rulebook = computed(() =>
  rulebooks.find(item => item.id === Number(route.params.id))
)
</script>
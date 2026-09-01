<template>
  <PageContainer>
    <Navbar />

    <div v-if="isLoading" class="d-flex justify-center mt-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <template v-else>
      <RulebookDetailsHero
        v-if="currentRulebook"
        :rulebook="currentRulebook"
      />

      <v-empty-state
        v-else
        title="Rulebook not found"
        icon="mdi-book-off-outline"
      >
        <template #actions>
          <BaseButton @click="router.push('/library')">← Back to Library</BaseButton>
        </template>
      </v-empty-state>

      <RulebookCarousel 
        v-if="recommendedBooks.length > 0"
        title="You might also like" 
        :rulebooks="recommendedBooks" 
      />
    </template>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useLibrary } from '~/composables/useLibrary'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import RulebookDetailsHero from '~/components/features/library/RulebookDetailsHero.vue'
import RulebookCarousel from '~/components/features/library/RulebookCarousel.vue'

const route = useRoute()

const { currentRulebook, rulebooks, isLoading, getRulebookById, getAllRulebooks } = useLibrary();

onMounted(async () => {
  const rulebookId = route.params.id;
  await getRulebookById(rulebookId);

  if(rulebooks.value.length === 0){
    await getAllRulebooks(); // populates carousel if empty (for now)
  }
})

const recommendedBooks = computed(() =>{
  if(!currentRulebook || !currentRulebook.value) return []
  if(!rulebooks || !rulebooks.value) return []
  return rulebooks.value.filter(item => item.id !== currentRulebook.value.id)
})
</script>
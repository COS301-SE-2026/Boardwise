<template>
  <div v-if="rulebook">
    <RulebookDetailsHero :rulebook="rulebook"/>
  </div>
  <!-- <PageContainer> -->

    <!-- <Navbar /> -->

    <!-- <v-row v-if="rulebook" class="mt-12" align="start">

      <v-col cols="12" md="4">
        <div style="position: sticky; top: 100px;">
          <BaseImage
            :src="rulebook.image"
            :alt="rulebook.gameName"
            height="480px"
            fit="cover"
            style="border-radius: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.15);"
          />
        </div>
      </v-col>

      <v-col cols="12" md="8">
        <div class="d-flex flex-column ga-6">

          <RulebookInfo :rulebook="rulebook" />

          <div class="d-flex flex-wrap ga-4">
            <BaseButton @click="goRead">
              <v-icon start>mdi-book-open-variant</v-icon>
              Read
            </BaseButton>
            <BaseButton variant="secondary" @click="router.push('/marketplace')">
              <v-icon start>mdi-store</v-icon>
              Browse Marketplace
            </BaseButton>
          </div>

          <BaseCard class="pa-6">
            <h3 class="text-h6 font-weight-bold mb-3">Description</h3>
            <p class="text-medium-emphasis mb-0" style="line-height: 1.8;">
              {{ rulebook.description }}
            </p>
          </BaseCard>

        </div>
      </v-col>

    </v-row> -->

    <v-empty-state
      v-else
      title="Rulebook not found"
      icon="mdi-book-off-outline"
    >
      <!-- <template #actions>
        <BaseButton @click="router.push('/library')">← Back to Library</BaseButton>
      </template> -->
    </v-empty-state>

    <!-- <RecommendedBooks :rulebooks="recommended" /> -->

  <!-- </PageContainer> -->
</template>

<script setup>


// import Navbar from '~/components/layout/Navbar.vue'
// import PageContainer from '~/components/layout/PageContainer.vue'
// import BaseButton from '~/components/ui/BaseButton.vue'
// import BaseCard from '~/components/ui/BaseCard.vue'
// import BaseImage from '~/components/ui/BaseImage.vue'

// import RulebookInfo from '~/components/features/library/RulebookInfo.vue'
// import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'
import RulebookDetailsHero from './RulebookDetailsHero.vue'

// const router = useRouter()
// const route = useRoute()

// const rulebook = computed(() =>
//   rulebooks.find(item => item.id === Number(route.params.id))
// )

// const recommended = computed(() =>
//   rulebooks
//     .filter(item => item.id !== Number(route.params.id))
//     .slice(0, 4)
// )

// const goRead = () => {
//   router.push(`/library/read/${rulebook.value.id}`)
// }

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>
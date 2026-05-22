<template>
  <PageContainer>
    <Navbar />

    <v-row v-if="rulebook" class="mt-12" align="start">

      <v-col cols="12" md="4">
        <div style="position: sticky; top: 100px;">
          <BaseImage
            :src="rulebook.image"
            :alt="rulebook.title"
            height="480px"
            fit="cover"
            style="border-radius: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.15);"
          />
        </div>
      </v-col>

      <v-col cols="12" md="8">
        <div class="d-flex flex-column ga-6">

          <div class="d-flex flex-column ga-2">
            <p class="text-caption text-uppercase font-weight-bold text-primary mb-0">
              {{ rulebook.category }}
            </p>
            <h1 class="text-h3 font-weight-bold" style="line-height: 1.1;">
              {{ rulebook.title }}
            </h1>
          </div>

          <div class="d-flex flex-wrap ga-4">
            <BaseButton @click="goRead">
              <v-icon start>mdi-book-open-variant</v-icon>
              Read Rulebook
            </BaseButton>
            <BaseButton variant="secondary" @click="goMarketplace">
              <v-icon start>mdi-store</v-icon>
              Browse Marketplace
            </BaseButton>
          </div>

          <BaseCard class="pa-7">
            <h3 class="text-h6 font-weight-bold mb-4">Description</h3>
            <p class="text-medium-emphasis mb-0" style="line-height: 1.8;">
              {{ rulebook.description }}
            </p>
          </BaseCard>

        </div>
      </v-col>

    </v-row>

    <v-empty-state
      v-else
      title="Rulebook not found"
      icon="mdi-book-off-outline"
    >
      <template #actions>
        <BaseButton @click="router.push('/library')">← Back to Library</BaseButton>
      </template>
    </v-empty-state>

    <RulebookCarousel title="You Might Also Like" :books="recommendedBooks" />

  </PageContainer>
</template>

<script setup>
import { rulebooks } from '~/services/mockData/rulebooks.js'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import RulebookCarousel from '~/components/features/library/RulebookCarousel.vue'

const route = useRoute()
const router = useRouter()

const rulebook = computed(() =>
  rulebooks.find(item => item.id === Number(route.params.id))
)

const recommendedBooks = computed(() =>
  rulebooks.filter(item => item.id !== Number(route.params.id))
)

const goRead = () => router.push(`/library/read/${rulebook.value.id}`)
const goMarketplace = () => router.push('/marketplace')
</script>
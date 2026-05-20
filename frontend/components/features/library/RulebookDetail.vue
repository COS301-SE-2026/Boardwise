<template>
  <PageContainer>

    <Navbar />

    <div v-if="rulebook" class="layout">

      <div class="left">

        <img
          :src="rulebook.image"
          :alt="rulebook.title"
          class="cover"
        />

      </div>

      <div class="right">

        <RulebookInfo :rulebook="rulebook" />

        <div class="buttons">

          <BaseButton @click="goRead">
            Read
          </BaseButton>

          <BaseButton variant="secondary">
            Browse Marketplace
          </BaseButton>

        </div>

        <ReaderContent :text="rulebook.description" />

      </div>

    </div>

    <RecommendedBooks
      :rulebooks="recommended"
    />

  </PageContainer>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import { rulebooks } from '~/services/mockData/rulebooks.js'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import RulebookInfo from '~/components/features/library/RulebookInfo.vue'
import ReaderContent from '~/components/features/library/ReaderContent.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'

import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()
const route = useRoute()

const rulebook = computed(() => {
  return rulebooks.find(
    item => item.id === Number(route.params.id)
  )
})

const recommended = computed(() => {
  return rulebooks
    .filter(item => item.id !== Number(route.params.id))
    .slice(0, 4)
})

const goRead = () => {
  router.push(`/rulebook/read/${book.value.id}`)
}
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 420px 1fr;

  gap: 56px;

  margin-top: 48px;
  align-items: start;
}

.left {
  position: sticky;
  top: 100px;
}

.cover {
  width: 100%;
  height: auto;

  border-radius: 28px;

  object-fit: cover;

  box-shadow:
    0 20px 40px rgba(0,0,0,0.15);
}

.right {
  display: flex;
  flex-direction: column;
}

.buttons {
  display: flex;
  gap: 16px;

  margin-top: 28px;
  margin-bottom: 32px;
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .left {
    position: relative;
    top: unset;
  }

  .cover {
    max-width: 420px;
  }
}

@media (max-width: 768px) {
  .layout {
    gap: 32px;
    margin-top: 32px;
  }

  .buttons {
    flex-direction: column;
    width: 100%;
  }

  .cover {
    max-width: 100%;
  }
}
</style>
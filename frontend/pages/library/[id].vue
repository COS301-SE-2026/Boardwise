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

        <div class="meta">
          <p class="category">{{ rulebook.category }}</p>
          <h1>{{ rulebook.title }}</h1>
        </div>

        <div class="actions">
          <BaseButton @click="goRead">Read Rulebook</BaseButton>
          <BaseButton variant="secondary" @click="goMarketplace">
            Browse Marketplace
          </BaseButton>
        </div>

        <div class="description">
          <h3>Description</h3>
          <p>{{ rulebook.description }}</p>
        </div>

      </div>

    </div>

    <div v-else class="not-found">
      <p>Rulebook not found.</p>
      <NuxtLink to="/library">← Back to Library</NuxtLink>
    </div>

    <RulebookCarousel
      title="You Might Also Like"
      :books="recommendedBooks"
    />

  </PageContainer>
</template>

<script setup>
import { rulebooks } from '~/services/mockData/rulebooks.js'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import RulebookCarousel from '~/components/features/library/RulebookCarousel.vue'

const route = useRoute()
const router = useRouter()

const rulebook = computed(() =>
  rulebooks.find(item => item.id === Number(route.params.id))
)

const recommendedBooks = computed(() =>
  rulebooks.filter(item => item.id !== Number(route.params.id))
)

const goRead = () => {
  router.push(`/library/read/${rulebook.value.id}`)
}

const goMarketplace = () => {
  router.push('/marketplace')
}
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 380px 1fr;
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
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.right {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.meta { display: flex; flex-direction: column; gap: 8px; }

.category {
  color: #6C3BFF;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
}

h1 { margin: 0; font-size: 48px; line-height: 1.1; }

.actions { display: flex; gap: 16px; flex-wrap: wrap; }

.description {
  background: white;
  padding: 28px;
  border-radius: 16px;
  border: 1px solid #eee;
}

.description h3 {
  margin: 0 0 16px;
  font-size: 18px;
}

.description p {
  margin: 0;
  color: #555;
  line-height: 1.8;
  font-size: 16px;
}

.not-found {
  text-align: center;
  margin-top: 80px;
  color: #666;
}

.not-found a {
  color: #6C3BFF;
  text-decoration: none;
  display: block;
  margin-top: 12px;
}

@media (max-width: 1100px) {
  .layout { grid-template-columns: 1fr; }
  .left { position: relative; top: unset; }
  .cover { max-width: 420px; }
}

@media (max-width: 768px) {
  .layout { gap: 32px; margin-top: 32px; }
  h1 { font-size: 36px; }
  .actions { flex-direction: column; }
  .cover { max-width: 100%; }
}
</style>
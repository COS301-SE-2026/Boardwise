<template>
  <PageContainer>

    <Navbar />

    <div class="header">
      <SectionTitle
        title="Library"
        subtitle="Browse community rulebooks"
      />

      <div class="search-row">
        <div class="search">
          <BaseSearch placeholder="Search rulebooks..." />
        </div>
      </div>

      <BaseTabs
        :tabs="tabs"
        :active-tab="selectedTab"
        @change="selectedTab = $event"
      />
    </div>

    <RecommendedBooks :rulebooks="recommended" />

    <SectionTitle title="All Rulebooks" />

    <RulebookGrid
      :rulebooks="filteredRulebooks"
      @select="openRulebook"
    />

    <BaseModal v-model="showModal">
      <RulebookDetails
        v-if="selectedRulebook"
        :rulebook="selectedRulebook"
      />
    </BaseModal>

  </PageContainer>
</template>

<script setup>
import { rulebooks } from '~/services/mockData/rulebooks.js'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

import RulebookGrid from '~/components/features/library/RulebookGrid.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'
import RulebookDetails from '~/components/features/library/RulebookDetail.vue'

const tabs = ['All', 'Strategy', 'Family', 'Party']
const selectedTab = ref('All')
const showModal = ref(false)
const selectedRulebook = ref(null)

const recommended = rulebooks.slice(0, 5)

const filteredRulebooks = computed(() => {
  if (selectedTab.value === 'All') return rulebooks
  return rulebooks.filter(r => r.category === selectedTab.value)
})

const openRulebook = (rulebook) => {
  selectedRulebook.value = rulebook
  showModal.value = true
}
</script>

<style scoped>
.header {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 8px;
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search { flex: 1; }
</style>
<template>
  <PageContainer>

    <Navbar />

    <PageHeader
      title="Library"
      subtitle="Browse community rulebooks"
    />

    <BaseSearch placeholder="Search rulebooks..." />

    <BaseTabs
      :tabs="tabs"
      v-model="selectedTab"
    />

    <!-- Recommended --> 
    <RecommendedBooks />

    <!-- Main Library Heading --> 
    <SectionTitle
      title="All Rulebooks"
      subtitle="Browse the full collection"
    />

    <!-- Actual Library -->
    <RulebookGrid />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import PageHeader from '~/components/layout/PageHeader.vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

import RulebookGrid from '~/components/features/library/RulebookGrid.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'

const tabs = [
  'All',
  'Strategy',
  'Family',
  'Party'
]

const selectedTab = ref('All')
</script>
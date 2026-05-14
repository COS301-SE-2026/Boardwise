<template>
  <PageContainer>

    <Navbar />

    <RecommendedBooks />

    <RulebookDetails :rulebook="rulebook" />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import RulebookDetails from '~/components/features/library/RulebookDetail.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'

import { books } from '~/services/mockData/rulebooks'

const route = useRoute()

const rulebook = rulebooks.find(
  item => item.id == route.params.id
)
</script>
<template>
  <PageContainer>

    <Navbar />

    <ReaderControls />

    <ReaderContent :rulebook="rulebook" />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ReaderControls from '~/components/features/library/ReaderControls.vue'
import ReaderContent from '~/components/features/library/ReaderContent.vue'

import { rulebooks } from '~/services/mockData/rulebooks'

const route = useRoute()

const rulebook = rulebooks.find(
  item => item.id == route.params.id
)
</script>
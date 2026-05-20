<template>
  <PageContainer>

    <Navbar />

    <ListingDetail
      v-if="listing"
      :listing="listing"
    />

  </PageContainer>
</template>

<script setup>
import { listings } from '~/services/mockData/listings'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import ListingDetail from '~/components/features/marketplace/ListingDetail.vue'

const route = useRoute()

const listing = listings.find(
  item => item.id === Number(route.params.id)
)
</script>
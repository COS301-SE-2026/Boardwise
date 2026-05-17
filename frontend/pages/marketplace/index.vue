<template>
  <PageContainer>

    <Navbar />

    <MarketplaceHeader @create-listing="showCreateListing = true" />

    <MarketplaceTabs v-model="activeTab" />

    <div class="marketplace-layout">

      <FilterSidebar />
      <ListingGrid :listings="listings" />
    </div>

    <AddListingModal v-model="showCreateListing" />

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import MarketplaceHeader from '~/components/features/marketplace/MarketplaceHeader.vue'
import MarketplaceTabs from '~/components/features/marketplace/MarketplaceTabs.vue'

import FilterSidebar from '~/components/features/marketplace/FilterSidebar.vue'
import ListingGrid from '~/components/features/marketplace/ListingGrid.vue'
import AddListingModal from '~/components/features/marketplace/AddListingModal.vue'

const activeTab = ref('Community')
const showCreateListing = ref(false)

const listings = ref([
  { id: 1, title: 'Catan',           type: 'rent', price: 50,   rentalPeriod: 'week', seller: 'PretoriaPlayer',  location: 'Pretoria',     image: '/images/catan.jpg'   },
  { id: 2, title: 'Wingspan',         type: 'sell', price: 350,                        seller: 'JburgGames',      location: 'Johannesburg', image: '/images/wingspan.jpg' },
  { id: 3, title: 'Dixit',            type: 'rent', price: 40,   rentalPeriod: 'week', seller: 'MeeplesMaster',   location: 'Cape Town',    image: '/images/dixit.jpg'   },
  { id: 4, title: 'Azul',             type: 'sell', price: 300,                        seller: 'BoardGameFan',    location: 'Centurion',    image: '/images/azul.jpg'    },
  { id: 5, title: 'Ticket to Ride',   type: 'rent', price: 60,   rentalPeriod: 'week', seller: 'RailroadRenter',  location: 'Pretoria',     image: '/images/ticket.jpg'  },
  { id: 6, title: '7 Wonders',        type: 'sell', price: 400,                        seller: 'AncientStrategy', location: 'Johannesburg', image: '/images/7wonders.jpg' },
  { id: 7, title: 'Terraforming Mars',type: 'rent', price: 70,   rentalPeriod: 'week', seller: 'MarsColonist',    location: 'Cape Town',    image: '/images/mars.jpg'    },
  { id: 8, title: 'Gloomhaven',       type: 'sell', price: 1200,                       seller: 'EpicAdventures',  location: 'Pretoria',     image: '/images/gloom.jpg'   },
])
</script>

<style scoped>
.marketplace-layout {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  align-items: flex-start;
}

@media (max-width: 900px) {
  .marketplace-layout {
    flex-direction: column;
  }
}
</style>
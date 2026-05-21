<template>
  <div class="d-flex align-center ga-4 flex-wrap mt-6">


    <div class="search">
      <BaseSearch
        placeholder="Search for games..."
      />
    </div>

    <v-btn variant="outlined" color="primary">Filters</v-btn>
    <v-btn color="primary">+ Create Listing</v-btn>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
</script>

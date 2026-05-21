<template>
  <v-card class="d-flex justify-space-around align-center pa-6 flex-wrap ga-6" style="margin-top: -40px;">

    <div class="text-center flex-grow-1">
      <h2 class="ma-0" style="font-size: 32px;">{{ games }}</h2>
      <p class="text-grey mt-2 ma-0">Games</p>
    </div>

    <v-divider vertical class="d-none d-sm-flex" style="height: 60px;" />

    <div class="text-center flex-grow-1">
      <h2 class="ma-0" style="font-size: 32px;">{{ friends }}</h2>
      <p class="text-grey mt-2 ma-0">Friends</p>
    </div>

    <v-divider vertical class="d-none d-sm-flex" style="height: 60px;" />

    <div class="text-center flex-grow-1">
      <h2 class="ma-0" style="font-size: 32px;">{{ communities }}</h2>
      <p class="text-grey mt-2 ma-0">Communities</p>
    </div>

  </v-card>
</template>

<script setup>
defineProps({
  games: Number,
  friends: Number,
  communities: Number,
  reviews: Number
})
</script>
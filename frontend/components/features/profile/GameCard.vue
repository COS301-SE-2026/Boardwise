<template>
  <v-card hover @click="showDetail = true" style="cursor: pointer; overflow: hidden;">

    <v-img :src="image" :alt="title" height="180" cover />

    <v-card-text>
      <h3 class="mb-2">{{ title }}</h3>
      <p class="text-grey">{{ category }}</p>
    </v-card-text>
    <!-- <RulebookDetail 
      v-model="showDetail"
      :game = "{ title, category, image }"
    /> -->

  </v-card>
</template>

<script setup>
// import RulebookDetail from '~/components/features/library/RulebookDetail.vue'

defineProps({
  title: String,
  category: String,
  image: String
})

const showDetail = ref(false)
</script>
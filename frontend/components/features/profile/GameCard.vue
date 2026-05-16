<template>
  <BaseCard class="card" 
    @click="showDetail = true"
    style="cursor: pointer;"
  >

    <img
      :src="image"
      :alt="title"
    />

    <div class="content">

      <h3>{{ title }}</h3>

      <p>{{ category }}</p>

    </div>

    <!-- <RulebookDetail 
      v-model="showDetail"
      :game = "{ title, category, image }"
    /> -->

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
// import RulebookDetail from '~/components/features/library/RulebookDetail.vue'

defineProps({
  title: String,
  category: String,
  image: String
})

const showDetail = ref(false)
</script>

<style scoped>
.card {
  overflow: hidden;
  padding: 0;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.content {
  padding: 16px;
}

h3 {
  margin: 0;
}

p {
  margin-top: 8px;
  color: #666;
}
</style>
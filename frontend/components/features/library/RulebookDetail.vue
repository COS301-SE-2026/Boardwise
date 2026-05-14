<template>
  <div class="details">

    <div class="image-section">
      <img :src="rulebook.image" :alt="rulebook.title" />
    </div>

    <div class="info-section">

      <SectionTitle
        :title="rulebook.title"
        :subtitle="rulebook.category"
      />

      <p class="description">
        {{ rulebook.description }}
      </p>

      <BaseButton @click="readBook">
        Read Rulebook
      </BaseButton>

    </div>

  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  rulebook: Object
})

const readBook = () => {
  navigateTo(`/library/read/${props.rulebook.id}`)
}
</script>

<style scoped>
.details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-top: 40px;
}

.image-section img {
  width: 100%;
  object-fit: contain;
  border-radius: 12px;
}

.description {
  margin: 20px 0;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .details {
    grid-template-columns: 1fr;
  }
}
</style>
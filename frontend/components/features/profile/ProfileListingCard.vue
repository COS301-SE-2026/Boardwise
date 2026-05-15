<template>
  <BaseCard class="card">

    <img
      :src="listing.image"
      :alt="listing.title"
    />

    <div class="content">

      <h3>{{ listing.title }}</h3>

      <p class="price">
        R{{ listing.price }}
      </p>

      <div class="actions">

        <BaseButton size="sm">
          Edit
        </BaseButton>

        <BaseButton
          size="sm"
          variant="secondary"
        >
          Delete
        </BaseButton>

      </div>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  listing: Object
})
</script>

<style scoped>
.card {
  overflow: hidden;
  padding: 0;
}

img {
  width: 100%;
  height: 180px;

  object-fit: cover;
}

.content {
  padding: 16px;
}

.price {
  color: #6C3BFF;
  font-weight: bold;

  margin-top: 8px;
}

.actions {
  display: flex;
  gap: 12px;

  margin-top: 16px;
}
</style>
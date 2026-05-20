<template>
  <section class="hero">

    <div class="image-wrapper">

      <img
        :src="rulebook.image"
        :alt="rulebook.title"
        class="cover"
      />

    </div>

    <div class="content">

      <RulebookMeta :rulebook="rulebook" />

      <RulebookDescription :rulebook="rulebook" />

      <RulebookActions :rulebook="rulebook" />

    </div>

  </section>
</template>

<script setup>
import RulebookMeta from './RulebookMeta.vue'
import RulebookDescription from './RulebookDescription.vue'
import RulebookActions from './RulebookActions.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 380px 1fr;

  gap: 48px;

  margin-top: 48px;
  align-items: start;
}

.image-wrapper {
  position: sticky;
  top: 100px;
}

.cover {
  width: 100%;
  border-radius: 28px;

  object-fit: cover;

  box-shadow:
    0 20px 40px rgba(0,0,0,0.15);
}

.content {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

@media (max-width: 1000px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .image-wrapper {
    position: relative;
    top: unset;
  }

  .cover {
    max-width: 420px;
  }
}
</style>
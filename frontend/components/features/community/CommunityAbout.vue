<template>
  <section class="section">

    <SectionTitle title="About" />

    <BaseCard class="about-card">

      <div class="row">
        <h3>Description</h3>
        <p>{{ community.description }}</p>
      </div>

      <div class="row">
        <h3>Type</h3>
        <BaseBadge>{{ community.type }}</BaseBadge>
      </div>

      <div class="row">
        <h3>Members</h3>
        <p>{{ community.members }} members</p>
      </div>

      <div class="row">
        <h3>Rules</h3>
        <p>Be respectful. Stay on topic. Have fun!</p>
      </div>

    </BaseCard>

  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

defineProps({
  community: { type: Object, required: true }
})
</script>

<style scoped>
.section {
  margin-top: 8px;
}

.about-card {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

h3 {
  margin: 0;
  font-size: 0.95rem;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

p {
  margin: 0;
  color: #333;
  line-height: 1.6;
}
</style>
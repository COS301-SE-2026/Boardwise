<template>
  <BaseCard class="card">

    <div class="avatar-container">
      <BaseAvatar :src="member.avatar" />
    </div>

    <div class="content">
      <h3>{{ member.name }}</h3>
      <p>{{ member.role }}</p>
    </div>

  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

defineProps({
  member: { type: Object, required: true }
})
</script>

<style scoped>
.card {
  overflow: hidden;
  padding: 0;
  cursor: pointer;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.avatar-container {
  width: 100%;
  height: 120px;
  background: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-container :deep(.avatar) {
  width: 72px;
  height: 72px;
}

.content {
  padding: 16px;
}

h3 {
  margin: 0;
}

p {
  margin-top: 6px;
  color: #666;
  font-size: 0.875rem;
}
</style>
<template>
  <div class="message" :class="{ 'message--own': message.isOwn }">

    <BaseAvatar :src="message.avatar" />

    <div class="message__body">

      <div class="message__meta">
        <span class="message__name">{{ message.name }}</span>
        <span class="message__time">{{ message.time }}</span>
      </div>

      <BaseCard class="bubble">
        <p>{{ message.text }}</p>
      </BaseCard>

    </div>

  </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  message: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message--own {
  flex-direction: row-reverse;
}

.message__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 60%;
}

.message--own .message__body {
  align-items: flex-end;
}

.message__meta {
  display: flex;
  gap: 8px;
  align-items: baseline;
}

.message--own .message__meta {
  flex-direction: row-reverse;
}

.message__name {
  font-weight: 600;
  font-size: 0.875rem;
  color: #111;
}

.message__time {
  font-size: 0.75rem;
  color: #999;
}

.bubble :deep(p) {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

.message--own .bubble {
  background: #6C3BFF;
}

.message--own .bubble :deep(p) {
  color: white;
}
</style>
<template>
  <div class="message" :class="{ 'message--own': message.isOwn }">

    <BaseAvatar :src="message.avatar" size="md"/>

    <div class="message__body">

      <div class="message__meta">
        <span class="message__name">{{ message.name }}</span>
        <span class="message__time">{{ message.time }}</span>
      </div>

      <div class="bubble">
        <p>{{ message.text }}</p>
      </div>

    </div>

  </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

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
  gap: var(--space-3);
  align-items: flex-start;
}

.message--own {
  flex-direction: row-reverse;
}

.message__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  max-width: 60%;
}

.message--own .message__body {
  align-items: flex-end;
}

.message__meta {
  display: flex;
  gap: var(--space-2);
  align-items: baseline;
}

.message--own .message__meta {
  flex-direction: row-reverse;
}

.message__name {
  font-weight: var(--fw-bold);
  font-size: var(--fs-small);
  color: var(--color-text);
}

.message__time {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.bubble {
  background: var(--color-surface-alt);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-2) var(--space-4);
  width: fit-content;
}

.bubble p {
  margin: 0;
  font-size: var(--fs-body);
  line-height: var(--lh-normal);
  color: var(--color-text);
}

.message--own .bubble {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.message--own .bubble p {
  color: var(--color-text-inverse);
}
</style>
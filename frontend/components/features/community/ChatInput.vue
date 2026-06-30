<template>
  <div class="chat-input">

    <BaseTextArea
      v-model="text"
      placeholder="Write a message..."
      :rows="2"
      @keydown.enter.prevent="handleSend"
      class="flex-1"
      hide-details
    />

    <BaseButton @click="handleSend"
      class="align-stretch h-auto"
    >
      Send
    </BaseButton>

  </div>
</template>

<script setup>
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const emit = defineEmits(['send'])

const text = ref('')

const handleSend = () => {
  if (!text.value.trim()) return
  emit('send', text.value.trim())
  text.value = ''
}
</script>

<style scoped>
.chat-input {
  display: flex;
  gap: var(--space-3);
  align-items: stretch;
}

.chat-input :deep(.base-textarea) {
  flex: 1;
  min-height: unset;
  resize: none;
}
</style>
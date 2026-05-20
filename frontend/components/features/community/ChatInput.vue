<template>
  <div class="chat-input">

    <BaseTextArea
      v-model="text"
      placeholder="Write a message..."
      :rows="2"
      @keydown.enter.prevent="handleSend"
    />

    <BaseButton @click="handleSend">
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
  gap: 12px;
  align-items: flex-end;
}

.chat-input :deep(.base-textarea) {
  flex: 1;
  min-height: unset;
  resize: none;
}
</style>
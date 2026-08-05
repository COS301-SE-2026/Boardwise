<template>
  <div class="d-flex ga-3 align-end" >

    <BaseTextArea
      v-model="text"
      placeholder="Write a message..."
      :rows="2"
      hide-details
      @keydown.enter.prevent="handleSend"
      class="flex-1"
    />

    <BaseButton @click="handleSend"
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

  const message = text.value.trim()

  if (!message) return

  emit('send', message)
  text.value = ''
}
</script>
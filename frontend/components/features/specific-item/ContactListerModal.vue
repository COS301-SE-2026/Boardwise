<template>
  <BaseModal v-model="open">
    <div class="content">

      <h2>Contact lister</h2>

      <div class="form">

        <v-text-field
          v-model="name"
          label="Your name"
          placeholder="e.g. Lesa Nkosi"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
        />

        <v-text-field
          v-model="email"
          label="Your email"
          placeholder="e.g. nkosi_lesa@gmail.com"
          type="email"
          variant="outlined"
          density="comfortable"
          hide-details="auto"
        />

        <v-textarea
          v-model="message"
          label="Message"
          :placeholder="`Hey, I'm interested in your listing for ${listingTitle}...`"
          variant="outlined"
          density="comfortable"
          :rows="4"
          hide-details="auto"
        />

      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="open = false">Cancel</BaseButton>
        <BaseButton @click="handleSend">Send message</BaseButton>
      </div>

    </div>
  </BaseModal>

  <v-snackbar
    v-model="showToast"
    color="success"
    :timeout="3000"
    location="top"
  >
    Message sent! The lister will be in touch soon.
  </v-snackbar>
</template>

<script setup>
import { ref } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()

const props = defineProps({
  listingTitle: {
    type: String,
    default: 'this item'
  }
})

const emit = defineEmits(['sent'])

const name = ref('')
const email = ref('')
const message = ref('')
const showToast = ref(false)

const handleSend = () => {
  if (!name.value.trim() || !message.value.trim()) return
  emit('sent', {
    name: name.value,
    email: email.value,
    message: message.value
  })
  open.value = false
  showToast.value = true
  name.value = ''
  email.value = ''
  message.value = ''
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
</style>
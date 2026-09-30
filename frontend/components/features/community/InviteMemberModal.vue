<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-6">
      <h2> Invite Member</h2>

      <BaseInput
        v-model="username"
        label="Username"
        placeholder="Enter member's username"
      />

      <v-select
        v-model="role"
        :items="roles"
        label="Role"
        variant="outlined"
        rounded="lg"
      />               

      <div class="d-flex justify-end ga-3">
        <BaseButton
          variant="secondary"
          @click="closeModal"
          >
          Cancel
        </BaseButton>

        <BaseButton @click="handleInvite">
          Send Invite
        </BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue';
import BaseInput from '~/components/ui/BaseInput.vue';
import BaseModal from '~/components/ui/BaseModal.vue';

const open = defineModel({
    type: Boolean,
    default: false
})

const emit = defineEmits(['confirm'])

const username = ref('')

const role = ref('Member')

const roles = [
  'Member',
  'Admin'
]

const closeModal = () => {
  open.value = false
  username.value = ''
  role.value = 'Member'
}

const handleInvite = () => {
  if (!username.value.trim()) return

  emit('confirm', {
    username: username.value.trim(),
    role: role.value
  })

  closeModal()
}
</script>

<template>
  <BaseModal v-model="open">
    <div class="content">
      <h2>Invite Member</h2>

      <div class="input-group">
        <label for="invite-email">Email Address</label>
        <BaseInput id="invite-email" v-model="email" placeholder="Enter member's email" />
      </div>

      <div class="input-group">
        <label for="invite-role">Role</label>
        <select id="invite-role" v-model="role" class="select">
          <option value="Member">Member</option>
          <option value="Moderator">Moderator</option>
          <option value="Admin">Admin</option>
        </select>
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleInvite">Send Invite</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const open = ref(props.modelValue)
const email = ref('')
const role = ref('Member')

watch(() => props.modelValue, (val) => {
  open.value = val
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

const closeModal = () => {
  open.value = false
  email.value = ''
  role.value = 'Member'
}

const handleInvite = () => {
  if (!email.value.trim()) return
  emit('confirm', { email: email.value.trim(), role: role.value })
  closeModal()
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.content h2 {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.input-group label {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.select {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--fs-body);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
}

.select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(109, 0, 55, 0.15);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}
</style>
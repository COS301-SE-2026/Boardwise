<template>
  <BaseModal v-model="open">

    <div class="content">

      <h2>Edit Profile</h2>

      <BaseInput
        v-model="name"
        placeholder="Name"
      />

      <BaseInput
        v-model="username"
        placeholder="Username"
      />

      <BaseInput
        v-model="location"
        placeholder="Location"
      />

      <BaseTextarea
        v-model="bio"
        placeholder="Bio"
      />

      <div class="actions">

        <BaseButton
          variant="secondary"
          @click="open = false"
        >
          Cancel
        </BaseButton>

        <BaseButton>
          Save Changes
        </BaseButton>

      </div>

    </div>

  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextarea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()

const name = ref('')
const username = ref('')
const location = ref('')
const bio = ref('')
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
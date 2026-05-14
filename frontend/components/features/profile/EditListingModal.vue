<template>
  <BaseModal v-model="open">

    <div class="modal-content">

      <h2>Edit Listing</h2>

      <BaseInput
        v-model="title"
        placeholder="Listing title"
      />

      <BaseInput
        v-model="price"
        placeholder="Price"
      />

      <BaseTextarea
        v-model="description"
        placeholder="Description"
      />

      <div class="actions">

        <BaseButton variant="secondary">
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
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextarea from '~/components/ui/BaseTextarea.vue'

const open = defineModel()

const title = ref('')
const price = ref('')
const description = ref('')
</script>

<style scoped>
.modal-content {
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
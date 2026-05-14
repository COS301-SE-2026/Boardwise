<template>
  <BaseModal v-model="open">

    <div class="modal-content">

      <h2>Delete Listing</h2>

      <p>
        Are you sure you want to delete this listing?
      </p>

      <div class="actions">

        <BaseButton variant="secondary">
          Cancel
        </BaseButton>

        <BaseButton variant="danger">
          Delete
        </BaseButton>

      </div>

    </div>

  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
</script>

<style scoped>
.modal-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
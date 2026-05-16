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

      <BaseTextArea
        v-model="description"
        placeholder="Description"
      />

      <div class="actions">

        <BaseButton variant="secondary"
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
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'

const open = defineModel()
const props = defineProps({
  listing: {
    type: Object,
  }
})
const title = ref(props.listing?.title ?? '')
const price = ref(props.listing?.price ?? '')
const description = ref(props.listing?.description ?? '')
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
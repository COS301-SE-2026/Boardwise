<template>
  <BaseModal v-model="open">

    <div class="content">

      <h2>Add Game</h2>

      <BaseInput
        v-model="title"
        placeholder="Game title"
      />

      <BaseInput
        v-model="genre"
        placeholder="Genre"
      />

      <BaseInput
        v-model="image"
        placeholder="Image URL"
      />

      <div class="actions">

        <BaseButton
          variant="secondary"
          @click="open = false"
        >
          Cancel
        </BaseButton>

        <BaseButton>
          Add Game
        </BaseButton>

      </div>

    </div>

  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()

const title = ref('')
const genre = ref('')
const image = ref('')
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
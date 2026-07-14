<template>
  <v-dialog v-model="open" max-width="400">
    <v-card class="delete-modal">

      <div class="modal-icon">
        <v-icon size="28" color="error">mdi-trash-can-outline</v-icon>
      </div>

      <h2>Remove Game From Your Profile</h2>
      <p>Are you sure you want to remove this game from your profile?</p>

     <div class="d-flex justify-center ga-3 mt-2">
      <v-btn variant="outlined" color="primary" @click="open = false">Cancel</v-btn>
      <v-btn color="error" @click="confirm">Delete</v-btn>
    </div>

    
    </v-card>
  </v-dialog>
</template>

<script setup>
const open = defineModel({ type: Boolean, default: false })
const emit = defineEmits(['confirm'])

const confirm = () => {
  emit('confirm')
  open.value = false
}
</script>

<style scoped>
.delete-modal {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  border-radius: var(--radius-lg) !important;
}

.modal-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: #fdecea;
  display: flex;
  align-items: center;
  justify-content: center;
}

h2 {
  margin: 0;
  font-size: var(--fs-h3);
}

p {
  margin: 0;
  font-size: var(--fs-body);
  color: var(--color-text-muted);
}
</style>

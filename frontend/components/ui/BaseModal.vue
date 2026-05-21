<template>
<<<<<<< HEAD
  <v-dialog
    :model-value="modelValue"
    max-width="1200"
    scrollable
    @update:model-value="$emit('update:modelValue', $event)"
  >

    <BaseCard>

      <slot />

    </BaseCard>

  </v-dialog>

=======
 <v-dialog 
  :model-value="modelValue"
  max-width="600"
  @update:model-value="$emit('update:modalValue', $event)"
 >
    <v-card rounded="lg" :elevation="0">
      <v-card-text>
        <slot />
      </v-card-text>
    </v-card>
 </v-dialog>
>>>>>>> 2b8f2eca3bb7123b3cde44e42e7fa55ec16fc9c7
</template>

<script setup>
defineProps({
  modelValue: Boolean
})

<<<<<<< HEAD
defineEmits(['update:modelValue'])
=======
defineEmits(['update:modalValue'])
>>>>>>> 2b8f2eca3bb7123b3cde44e42e7fa55ec16fc9c7
</script>
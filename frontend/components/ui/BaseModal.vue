<template>
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
</template>

<script setup>
defineProps({
  modelValue: Boolean
})

defineEmits(['update:modalValue'])
</script>
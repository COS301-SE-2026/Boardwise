<template>
  <v-list-item class="px-0 py-2">
    <div class="d-flex align-center justify-space-between ga-4 w-100">
      <div class="flex-grow-1">
        <div
          :id="`${id}-label`"
          class="text-body-1 font-weight-medium"
        >
          {{ label }}
        </div>

        <div
          v-if="description"
          :id="`${id}-description`"
          class="text-body-2 text-medium-emphasis mt-1"
        >
          {{ description }}
        </div>
      </div>

      <v-switch
        :model-value="modelValue"
        color="primary"
        hide-details
        :aria-labelledby="`${id}-label`"
        :aria-describedby="
          description
            ? `${id}-description`
            : undefined
        "
        @update:model-value="$emit('update:modelValue', $event)"
      />
    </div>
  </v-list-item>
</template>

<script setup>
defineProps({
  id: {
    type: String,
    required: true
  },

  label: {
    type: String,
    required: true
  },

  description: {
    type: String,
    default: ''
  },

  modelValue: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue'])
</script>
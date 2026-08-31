<template>
  <v-text-field 
    v-model="inputValue" 
    :label="label"
    :type="resolvedType"
    :rules="rules"
    validate-on="input"
    hide-details="auto"
    v-bind="$attrs" 
  />
  <template
    v-if="isPassword"
    #append-inner
  >
  <v-btn
        :icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
        variant="text"
        density="compact"
        :aria-label="showPassword ? 'Hide password' : 'Show password'"
        :aria-pressed="showPassword ? 'true' : 'false'"
        @click="showPassword = !showPassword"
      />
    </template>
</template>

<script setup>
import { ref, computed } from 'vue'

defineOptions({ inheritAttrs: false })

const props = defineProps({
  label: { 
    type: String, 
    default: ''
  }, 
  type: {
    type: String,
    default: ''
  },
  rules: {
    type: String,
    default: 
  }
})

const inputValue = defineModel()
const showPassword = ref(false)
const isPassword = computed(() => props.type === 'password')

const resolvedType = computed(() => {
  if (!isPassword.value) {
    return props.type
  }

  return showPassword.value
    ? 'text'
    : 'password'
})
</script>

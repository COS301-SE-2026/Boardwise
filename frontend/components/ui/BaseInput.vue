<template>
  <v-text-field 
    v-model="inputValue" 
    :label="label"
    :type="isPassword && showPassword ? 'text' : type"
    :rules="rules"
    validate-on="input"
    hide-details="auto"
    :append-inner-icon="isPassword ? (showPassword ? 'mdi-eye-off' : 'mdi-eye') : undefined"
    @click:append-inner="showPassword =!showPassword"
    v-bind="$attrs" 
  />
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
    default: ''
  }
})

const inputValue = defineModel()
const showPassword = ref(false)
const isPassword = computed(() => props.type === 'password')
</script>

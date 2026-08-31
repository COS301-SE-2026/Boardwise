<template>
  <v-text-field
    v-model="inputValue"
    :label="label"
    :aria-label="accessibleLabel"
    :type="resolvedType"
    :rules="normalizedRules"
    validate-on="input"
    hide-details="auto"
    v-bind="$attrs"
  >
    <template #append-inner>
      <v-btn
        v-if="isPassword"
        :icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
        variant="text"
        density="compact"
        :aria-label="passwordToggleLabel"
        :aria-pressed="showPassword"
        @click="togglePassword"
      />
    </template>
  </v-text-field>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

defineOptions({
  inheritAttrs: false
})

const props = defineProps({
  label: {
    type: String,
    default: ''
  },

  ariaLabel: {
    type: String,
    default: ''
  },

  type: {
    type: String,
    default: 'text'
  },

  rules: {
    type: Array,
    default: () => []
  }
})

const inputValue = defineModel<string>({
  default: ''
})

const showPassword = ref(false)

const isPassword = computed(() => {
  return props.type === 'password'
})

const resolvedType = computed(() => {
  if (!isPassword.value) {
    return props.type
  }

  return showPassword.value
    ? 'text'
    : 'password'
})

const accessibleLabel = computed(() => {
  if (props.label) {
    return undefined
  }

  if (props.ariaLabel) {
    return props.ariaLabel
  }

  return 'Text input'
})

const passwordToggleLabel = computed(() => {
  return showPassword.value
    ? 'Hide password'
    : 'Show password'
})

const normalizedRules = computed(() => {
  return Array.isArray(props.rules)
    ? props.rules
    : []
})

const togglePassword = () => {
  showPassword.value = !showPassword.value
}
</script>
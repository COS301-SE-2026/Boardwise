<template>
  <v-text-field
    v-model="inputValue"
    class="base-input"
    :label="label"
    :rules="props.rules"
    :aria-label="accessibleLabel"
    :type="resolvedType"
    variant="outlined"
    density="comfortable"
    rounded="xl"
    validate-on="input"
    hide-details="auto"
    v-bind="$attrs"
  >
    <template #append-inner>
      <BaseButton
        v-if="isPassword"
        class="base-input__password-toggle"
        :icon="passwordIcon"
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
import BaseButton from './BaseButton.vue'

defineOptions({
  inheritAttrs: false
})

type InputRule = (value: any) => boolean | string

const props = withDefaults(defineProps<{
    label?: string
    rules?: InputRule[]
    ariaLabel?: string
    type?: string
}>(), {
  label: '',
  rules: () => [],
  ariaLabel: '',
  type: 'text'
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

const passwordIcon = computed<any>(() => 
  showPassword.value ? 'mdi-eye-off' : 'mdi-eye'
)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}
</script>
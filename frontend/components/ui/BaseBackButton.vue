<template>
  <BaseButton
    back
    variant="text"
    v-bind="$attrs"
    @click="handleClick"
  >
    <slot />
  </BaseButton>
</template>

<script setup>
import { useRouter } from 'vue-router'
import BaseButton from './BaseButton.vue'

const props = defineProps({
  to: {
    type: [String, Object],
    default: null
  }
})

const emit = defineEmits(['click'])

const router = useRouter()

const handleClick = (event) => {
  emit('click', event)
  if (props.to) {
    router.push(props.to)
  } else {
    router.back()
  }
}
</script>
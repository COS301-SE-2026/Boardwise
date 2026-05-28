<template>
  <v-text-field v-bind="$attrs" v-model="inputValue" />
</template>

<script setup>
defineOptions({ inheritAttrs: false })
const inputValue = defineModel()
</script>

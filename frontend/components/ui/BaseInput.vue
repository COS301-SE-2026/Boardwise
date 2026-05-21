<template>
  <v-text-field 
    :placeholder="placeholder"
    :type="type"
    v-model:model-value="inputValue"
  />

</template>

<script setup> 
const inputValue = defineModel()
</script>
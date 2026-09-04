<template>
  <v-chip
    class="base-tag"
    :variant="selected ? 'elevated' : 'outlined'"
    :color="selected ? 'primary': undefined"
    :class="{'base-tag--clickable': clickable}"
    @click="onClick"
  >
    <slot/>
  </v-chip>
</template>

<script setup>
const props = defineProps({
  selected: {
    type: Boolean,
    default: false
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

function onClick(event){
  if(props.clickable){
    emit('click', event)
  }
}
</script>
<style scoped>
.base-tag{
  font-weight: 500;
  transition: all 0.2s ease;
}
.base-tag--clickable{
  cursor: pointer;
}
.base-tag--clickable:hover{
  filter: brightness(0.95)
}
</style>
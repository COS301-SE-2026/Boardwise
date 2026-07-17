<template>
  <v-img 
    :src="src"
    :alt="alt"
    :height="height"
    :width="width"
    :cover="fit === 'cover'"
    v-bind="$attrs"  
  >
    <template #error>
      <div class="d-flex align-center justify-center fill-height bg-grey-lighten-3">
        <span class="text-caption text-grey"> 
          {{  alt ?? 'Image' }}
        </span>
      </div>
    </template>
  </v-img>
</template>

<script setup>
defineProps({
  src: String,
  alt: String,
  height: {
    type: String,
    default: '200px'
  },
  width: {
    type: String,
    default: '100%'
  },
  fit: {
    type: String,
    default: 'cover'
  }
})
</script>
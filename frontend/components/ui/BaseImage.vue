<template>
  <v-img 
    :src="src"
    :alt="alt"
    :height="height"
    :width="width"
    :cover="fit === 'cover'"
    :rounded="rounded"
    v-bind="$attrs"  
  >
    <template #error>
      <div class="base-image__fallback d-flex align-center justify-center fill-height bg-grey-lighten-3">
        <span class="text-caption text-grey"> 
          {{  alt ?? 'Image' }}
        </span>
      </div>
    </template>
  </v-img>
</template>

<script setup>
defineProps({
  src: {
    type: String,
    default: ' '
  },
  alt: {
    type: String,
    default: ' '
  },
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
  },
  rounded: {
    type: String,
    default: '0'
  }
})
</script>
<template> 
  <BaseCard clickable @click="$emit('click', rulebook)"> 
    <template #media>
      <BaseImage 
        :src="rulebook.coverUrl" 
        :alt="rulebook.title" 
      />
    </template>
    
    <p class="card-title">
      {{ rulebook.title }}
    </p>
    <p class="card-meta">
      {{ rulebook.edition }}
    </p>
    
  </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    rulebook: Object
})

defineEmits(['click'])
</script>
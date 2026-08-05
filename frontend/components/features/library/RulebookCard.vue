<template> 
  <BaseCard 
    class="cursor-pointer" 
    @click="$emit('click', rulebook)"
  > 

  <BaseImage 
    :src="rulebook.coverUrl" 
    :alt="rulebook.title" 
  />

  <div class="pa-4 d-flex flex-column ga-2">
    <h3 class="card-title">
      {{ rulebook.title }}
    </h3>

    <p class="card-meta">
      {{ rulebook.genre }}
    </p>
  </div>
        
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
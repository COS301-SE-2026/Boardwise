<template> 
    <BaseCard class="cursor-pointer overflow-hidden pa-0" 
    @click="$emit('click', rulebook)"> 

        <BaseImage :src="rulebook.image" :alt="rulebook.title" height="200px" fit="cover" />

        <div class="pa-3">
          <p class="text-body-2 font-weight-bold mb-1">
            {{ rulebook.title }}
          </p>

          <p class="text-caption text-medium-emphasis mb-0">
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
<template> 
  <BaseCard clickable class="h-100 d-flex flex-column" @click="$emit('click', rulebook)"> 
    <template #media>
      <BaseImage 
        :src="rulebook.coverUrl" 
        :alt="rulebook.title" 
        :height="size === 'sm' ? '120px' : '200px'"
      />
    </template>
    
    <div
      class="d-flex flex-column flex-grow-1"
      :class="size === 'sm' ? 'pa-3 ga-1' : 'pa-4 ga-2'"
    >
      <p class="card-title">
        {{ rulebook.title }}
      </p>
      <p class="card-meta">
        {{ rulebook.edition }}
      </p>
    </div>
  </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    rulebook: {
      type: Object, 
      required: true
    },
    size: {
      type: String, 
      default: 'lg',
      validator: (v) => ['sm', 'lg'].includes(v)
    }
})

defineEmits(['click'])
</script>
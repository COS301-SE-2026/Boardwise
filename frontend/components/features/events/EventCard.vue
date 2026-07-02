<template> 
    <BaseCard 
    class="cursor-pointer overflow-hidden pa-0"
    >
        <BaseImage
            :src="event.image ?? '/default-event.png'"
            :alt="event.title"
            height="200px"
            fit="cover"
        />

        <div class="pa-3">
          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.title }}
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.date }} 
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.time }} 
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.location }}
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.game }}
          </p> 
          
          <p class="text-caption text-medium-emphasis mb-1">
            Hosted By {{ event.host }}
          </p>
        </div>
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    event: {
        type: Object,
        required: true
    }
})

defineEmits(['click'])
</script>
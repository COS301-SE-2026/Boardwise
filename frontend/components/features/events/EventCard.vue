<template> 
    <BaseCard 
    class="cursor-pointer overflow-hidden pa-0"
    >
        <BaseImage
            :src="event.image ?? '/default-event.png'"
            :alt="event.name"
            height="200px"
            fit="cover"
        />

        <div class=" pa-4 d-flex flex-column ga-1
        ">
          <p class="text-subtitle-1 font-weight-bold
          ">
            {{ event.name }}
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.game }}
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.time }}  | {{  event.date }}
          </p>

          <p class="text-caption text-medium-emphasis mb-1">
            {{ event.location }}
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
<template>
    <BaseCard class=" d-flex flex-column ga-6 pa-6"
     >

      <BaseInput    class="pa-0 d-flex flex-column ga-2"
       >
        <h3 class="text-uppercase"
        >Description</h3>
        <p>{{ community.description }}</p>
      </BaseInput>

      <BaseInput  class="pa-0 d-flex flex-column ga-2">
        <h3 class="text-uppercase"
        >Members</h3>
        <p>{{ community.members }} members</p>
      </BaseInput>

      <BaseInput  class="pa-0 d-flex flex-column ga-2"
      >
        <h3 class="text-uppercase"
        >Rules</h3>
        <p>Be respectful. Stay on topic. Have fun!</p>
      </BaseInput>

    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseInput from '~/components/ui/BaseInput.vue';

defineProps({
  community: { 
    type: Object, 
    required: true 
  }
})
</script>

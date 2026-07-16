<template>
  <BaseCard class="pa-4">

    <div class="d-flex flex-column align-center ga-4 text-center">
      <BaseAvatar
       :src="member.avatar"
       :name="member.name"
       size="xl"
      />
      
    <div class="d-flex flex-column ga-1" >
      <h3 class="ma-0"
      >{{ member.name }}</h3>

      <p class="text-medium-emphasis ma-0"
      >{{ member.role }}</p>
    </div>
  </div>
  </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

defineProps({
  member: { 
    type: Object,
     required: true }
})
</script>

<template>
  <NuxtLink
    :to="`/profile/${encodeURIComponent(member.username)}`"
    class="community-member-link"
    :aria-label="`View ${member.username}'s profile`"
  >
  <BaseCard class="pa-4">

    <div class="d-flex flex-column align-center ga-4 text-center">
      <BaseAvatar
       :src="member.profilePicture ?? '/images/avatar.jpg'"
       :name="member.username"
       size="xl"
      />
      
    <div class="d-flex flex-column ga-1" >
      <h3 class="ma-0"
      >{{ member.username }}</h3>
    </div>
  </div>
  </BaseCard>
  </NuxtLink>
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

<template>
  <section v-if="modelValue">

    <BaseGrid cols="180px" >
      <MemberCard
        v-for="member in community.members"
        :key="member.username"
        :member="member"
      />
    </BaseGrid>

     <InviteMemberModal
      v-model="showInvite"
      @confirm="handleInvite"
    /> 
  </section>
</template>

<script setup>
import MemberCard from './MemberCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

import InviteMemberModal from './InviteMemberModal.vue'
defineProps({
  community: { type: Object, required: true },
  modelValue: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])

const showInvite = ref(false)

const handleInvite = (invite) => {
  console.log('Invite:', invite)
}

</script>

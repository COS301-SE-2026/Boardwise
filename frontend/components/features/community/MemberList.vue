<template>
   <section v-if="modelValue">
    <div class="d-flex justify-end mb-4">
      <BaseButton
        v-if="community.isMember"
        @click="showInvite = true"
      >
        <v-icon
          icon="mdi-account-plus-outline"
          class="me-2"
          aria-hidden="true"
        />

        Invite member
      </BaseButton>
    </div>

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
import BaseButton from '~/components/ui/BaseButton.vue'
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

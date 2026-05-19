<template>
  <section class="section">

    <div class="top">
      <SectionTitle title="Members" />
      <BaseButton @click="showInvite = true">+ Invite Member</BaseButton>
    </div>

    <div class="members-grid">
      <MemberCard
        v-for="member in community.members_list"
        :key="member.id"
        :member="member"
      />
    </div>

     <InviteMemberModal
      v-model="showInvite"
      @confirm="handleInvite"
    />
  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import MemberCard from './MemberCard.vue'
import InviteMemberModal from './InviteMemberModal.vue'

defineProps({
  community: { type: Object, required: true }
})

const showInvite = ref(false)

const handleInvite = (invite) => {
  console.log('Invite:', invite)
}

</script>

<style scoped>
.section {
  margin-top: 8px;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .top {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>s
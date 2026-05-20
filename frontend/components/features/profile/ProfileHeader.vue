<template>
  <BaseCard class="header">

    <div class="profile-top">

      <div class="left">

        <BaseAvatar
          :src="user.profilePicture ?? '/images/avatar.jpg'"
          size="xl"
        />

        <div class="info">

          <h1>{{ user.fullName }}</h1>

          <p class="username">
            @{{ user.username }}
          </p>

          <p class="bio" v-if="user.preferences.visibility === 'public'" v-for="genre in user.preferences.genres">
            {{ genre }}
          </p>

        </div>

      </div>

      <BaseButton @click="showEdit = true">
        Edit Profile
      </BaseButton>

      <EditProfileModal  v-model="showEdit" :user="user" @save="$emit('save', $event)"/>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import EditProfileModal from './EditProfileModal.vue';

defineProps({
  user: {
    type: Object,
    required: true
  }
})

const showEdit = ref(false)
</script>

<style scoped>
.header {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32px;
    border-radius: 24px;
    background: #F9F4E3;
}

.profile-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  gap: 24px;
  align-items: center;
}

.info h1 {
  margin: 0;
  font-size: 42px;
}

.username {
  color: #6C3BFF;
  font-weight: bold;
  margin-top: 8px;
}

.bio {
  color: #555;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .profile-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }

  .left {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style> 
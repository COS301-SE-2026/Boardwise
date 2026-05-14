<template>
  <BaseCard class="header">

    <div class="profile-top">

      <div class="left">

        <BaseAvatar
          :src="avatar"
          size="xl"
        />

        <div class="info">

          <h1>{{ name }}</h1>

          <p class="username">
            @{{ username }}
          </p>

          <p class="bio">
            {{ bio }}
          </p>

        </div>

      </div>

      <BaseButton>
        Edit Profile
      </BaseButton>

    </div>

  </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
  name: String,
  username: String,
  bio: String,
  avatar: String
})
</script>

<style scoped>
.header {
  padding: 40px;

  background: #FDF6E3;
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
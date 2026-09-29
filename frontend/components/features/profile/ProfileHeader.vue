<template>
  <BaseCard flush class="profile-header">

    <div class="profile-header__content">

      <!-- Identity -->
      <div class="profile-identity">

        <button 
          class="profile-avatar-button"
          type="button"
          aria-label="Change profile picture"
          @click="showPfpModal = true"
        >
          <BaseAvatar 
            :src="user.profilePicture ?? '/images/avatar.jpg'"
            :name="user.fullName"
            size="xxl"
            class="profile-avatar"
          />

          <span class="profile-avatar-edit">
            <v-icon size="15">mdi-camera</v-icon>
          </span>
        </button>
          
        <div class="profile-details">

          <div class="profile-name-row">
            <h1 class="profile-name">{{ user.fullName }}</h1>
            <BaseBadge v-if="user.role" size="small" variant="default">
              {{ user.role }}
            </BaseBadge>
          </div>
          
          <p class="profile-username">@{{ user.username }}</p>
            
          <div class="profile-preferences">

            <span class="preference-label">Favourite genres</span>

              <div
                v-if="user.preferences.genres?.length"
                class="preferences-badges"
              >
                <BaseBadge
                  v-for="genre in user.preferences.genres"
                  :key="genre"
                  size="small"
                  variant="default"
                >
                  {{ genre }}
                </BaseBadge>
              </div>
              
              <p v-else-if="user.preferences?.visibility === 'private'" class="no-pref">
                Genre preferences are private
              </p>

              <p v-else class="no-pref">
                No preferences added
              </p>

          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="profile-actions">
        <BaseButton @click="showEdit = true">
          <v-icon start size="17">mdi-pencil</v-icon>
          Edit Profile
        </BaseButton>
      </div>

    </div>

    <EditProfileModal
      v-model="showEdit"
      :user="user"
      @save="$emit('saved', $event)"
    />

    <ChangeProfilePictureModal
      v-model="showPfpModal"
      :user="user"
      @save="$emit('pfp-change', $event)"
    />

  </BaseCard>
</template>

<script setup>
import { ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

import ChangeProfilePictureModal from './ChangeProfilePictureModal.vue'
import EditProfileModal from './EditProfileModal.vue'

defineProps({
  user: { type: Object, required: true }
})

defineEmits(['saved', 'pfp-change'])

const showEdit = ref(false)
const showPfpModal = ref(false)

</script>
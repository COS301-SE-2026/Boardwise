<template>
  <BaseCard flush class="profile-header pa-10 w-100 ">

    <div class="d-flex justify-space-between align-center flex-wrap ga-6">

      <div class="d-flex align-center ga-6 flex-wrap profile-info">

        <BaseAvatar 
          :src="user.profilePicture ?? '/images/avatar.jpg'"
          :name="user.fullName"
          size="xxl"
          class="profile-avatar"
          @click="showPfpModal = true"
        />

        <div class="d-flex flex-column ga-3 profile-details">

          <h1 class="profile-name ma-0">{{ user.fullName }}</h1>

         
          <p class="profile-username ma-0">@{{ user.username }}</p>
          
          <div class="profile-preferences">

            <span class="preference-label">Preferences</span>

            <div
              v-if="user.preferences?.visibility === 'public' && user.preferences.genres?.length > 0"
              class="d-flex flex-wrap ga-1"
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
            
            <div v-else-if="user.preferences?.visibility === 'private'">
              <p class = "no-pref">user genre preferences are private</p>
            </div>

            <div v-else>
              <p class = "no-pref">no preferences</p>
            </div>

          </div>
        </div>

      </div>

      <BaseButton @click="showEdit = true">Edit Profile</BaseButton>

    </div>

    <EditProfileModal
      v-model="showEdit"
      :user="user"
      @save="$emit('saved', $event)"
    />

    <ChangeProfilePictureModal
      v-model="showPfpModal"
      :user="user"
      @save="$emit('pfpChange', $event)"
    />

  </BaseCard>
</template>

<script setup>
import { ref } from 'vue'
import EditProfileModal from './EditProfileModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import ChangeProfilePictureModal from './ChangeProfilePictureModal.vue';
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseBadge from '~/components/ui/BaseBadge.vue';

defineProps({
  user: { type: Object, required: true }
})

defineEmits(['saved', 'pfpChange'])

const showEdit = ref(false)
const showPfpModal = ref(false)

</script>
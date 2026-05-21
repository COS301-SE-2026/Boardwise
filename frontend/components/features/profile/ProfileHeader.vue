<template>
  <v-card class="pa-8 rounded-xl w-100" color="#F9F4E3" flat>

    <div class="d-flex justify-space-between align-center flex-wrap ga-6">

      <div class="d-flex align-center ga-6 flex-wrap">

        <v-avatar size="80">
          <v-img :src="user.profilePicture ?? '/images/avatar.jpg'" cover />
        </v-avatar>

        <div class="d-flex flex-column ga-2">
          <h1 class="ma-0" style="font-size: 42px;">{{ user.fullName }}</h1>
          <p class="text-primary font-weight-bold ma-0">@{{ user.username }}</p>
          <template v-if="user.preferences?.visibility === 'public'">
            <p v-for="genre in user.preferences.genres" :key="genre" class="text-grey ma-0">
              {{ genre }}
            </p>
          </template>
        </div>

      </div>

      <v-btn color="primary" @click="showEdit = true">Edit Profile</v-btn>

      <EditProfileModal v-model="showEdit" :user="user" @save="$emit('save', $event)" />

    </div>

  </v-card>
</template>

<script setup>
import EditProfileModal from './EditProfileModal.vue'

defineProps({
  user: {
    type: Object,
    required: true
  }
})

defineEmits(['save'])

const showEdit = ref(false)
</script>
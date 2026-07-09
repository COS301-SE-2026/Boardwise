<template>
  <v-card flat class="profile-header pa-10 w-100 ">

    <div class="d-flex justify-space-between align-center flex-wrap ga-6">

      <div class="d-flex align-center ga-6 flex-wrap">

        <v-avatar size="80" class="profile-avatar">
          <v-img
            :src="user.profilePicture ?? '/images/avatar.jpg'"
            :alt="`${user.fullName} profile picture`"
            cover
          />
        </v-avatar>

        <div class="d-flex flex-column ga-3">

          <h1 class="profile-name ma-0">{{ user.fullName }}</h1>

          <!-- <p class="profile-bio profile-bio--empty ma-0">no available preferences</p> -->
          <p class="profile-username ma-0">@{{ user.username }}</p>
          
          <div
            v-if="user.preferences?.visibility === 'public' && user.preferences.genres?.length > 0"
            class="d-flex flex-wrap ga-1"
          >
            <v-chip
              v-for="genre in user.preferences.genres"
              :key="genre"
              size="small"
              class="genre-chip"
            >
              {{ genre }}
            </v-chip>
            
          </div>
          
          <div v-else-if="user.preferences?.visibility === 'private'">
            <p class = "no-pref">user genre preferences are private</p>
          </div>

          <div v-else>
            <p class = "no-pref">no preferences</p>
          </div>

        </div>

      </div>

      <v-btn @click="showEdit = true">Edit Profile</v-btn>

    </div>

    <EditProfileModal
      v-model="showEdit"
      :user="user"
      @save="$emit('saved', $event)"
    />

  </v-card>
</template>

<script setup>
import EditProfileModal from './EditProfileModal.vue'

defineProps({
  user: { type: Object, required: true }
})

defineEmits(['saved'])

const showEdit = ref(false)
</script>

<style scoped>
.profile-header {
  background:    var(--color-surface-alt) !important;
  border-radius: var(--radius-lg) !important;
  border:        1px solid var(--color-border);
  box-shadow:    var(--shadow-sm) !important;
  min-height: 197px; 
}

.no-pref{

}
.profile-avatar {
  border: 3px solid var(--color-border-strong);
}

.profile-bio--empty {
  color:      var(--color-text-muted);
  font-style: italic;
}

.profile-name {
  font-family:  var(--font-display);
  font-size:    var(--fs-h2);
  font-weight:  var(--fw-regular);
  color:        var(--color-secondary);
  line-height:  var(--lh-tight);
}

.profile-username {
  font-family: var(--font-body);
  font-size:   var(--fs-body);
  font-weight: var(--fw-bold);
  color:       var(--color-primary);
  /* margin-bottom: 90px */
}

.profile-bio {
  font-family: var(--font-body);
  font-size:   var(--fs-body);
  color:       var(--color-text-muted);
  line-height: 4px;

}

.genre-chip {
  font-family:  var(--font-body) !important;
  font-size:    var(--fs-small) !important;
  font-weight:  var(--fw-medium) !important;
  background:   var(--bw-gold-muted) !important;
  color:        var(--bw-navy-ink) !important;
  border-radius: var(--radius-pill) !important;
}

:deep(.v-btn) {
  font-family:    var(--font-button) !important;
  background:     var(--color-primary) !important;
  color:          var(--color-text-inverse) !important;
  border-radius:  var(--radius-md) !important;
  height:         44px !important;
  padding:        0 var(--space-5) !important;
  text-transform: none !important;
  letter-spacing: 0 !important;
  box-shadow:     none !important;
}

:deep(.v-btn:hover) {
  background: var(--color-primary-hover) !important;
}
</style>
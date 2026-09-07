<template>
  <BaseModal v-model="open">

    <div class="content">
      <h2>Edit Profile</h2>

      <BaseInput
        v-model="name"
        placeholder="Name"
      />

      <BaseInput
        v-model="username"
        placeholder="Username"
      />

      <BaseInput
        v-model="location"
        placeholder="Location"
      />

      <BaseTextarea
        v-model="bio"
        placeholder="Bio"
      />

      <div class="preferences-section">
        <h3>Preferences</h3>
        
        <!-- Visibility -->
         <v-radio-group
          v-model="visibility"
          label="Who can see your prefernces?"
          inline
        >
          <v-radio label="Public" value="public" />
          <v-radio label="Private" value="private" />
        </v-radio-group>

        <!-- Genres -->
        <v-select
          v-model="selectedGenres"
          :items="genres"
          label="Preferred Genres"
          variant="outlined"
          multiple
          chips
          closable-chips
          hint="Select the genres you enjoy"
          persistent-hint
        />
      </div>

      <div class="actions">

        <BaseButton
          variant="secondary"
          @click="open = false"
          
        >
          Cancel
        </BaseButton>

        <BaseButton 
        @click="handleSave"
        :disabled="isLoading"
        >
          {{ isLoading ? 'Saving...' : 'Save Changes' }}
        </BaseButton>

      </div>

    </div>

  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextarea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { ref, watch } from 'vue'

import { useProfile } from '~/composables/useProfile'
import { useSnackBar } from '~/composables/useSnackbar';


const { updateProfile, isLoading, error, getGenres } = useProfile();
const { show } = useSnackBar();

const open = defineModel()
const emit = defineEmits(['save'])

const props = defineProps({
  user : {
    type: Object,
    required : true
  }
})

const name = ref('')
const username = ref('')
const location = ref('')
const bio = ref('')

const visibility = ref('public')

const genres = ref([])
const selectedGenres = ref([])

const loadUserData = () => {
  name.value = props.user.fullName ?? ''
  username.value = props.user.username ?? ''
  location.value = props.user.location ?? ''
  bio.value = props.user.bio ?? ''
  visibility.value = props.user.preferences?.visibility ?? 'public'
  selectedGenres.value = [...(props.user.preferences?.genres ?? [])]
}

const loadGenres = async () => {
  try {
    const res = await getGenres()
    genres.value = res
  } catch (err)
  {
    console.error('Failed to load genres', err)
  }
}

watch(open, async (isOpen) => {
  if(isOpen) {
    loadUserData()
    await loadGenres()
  }
})

const handleSave = async () => {
  try{
    const response = await updateProfile({
      name: name.value,
      username: username.value,
      location: location.value,
      // bio: bio.value,
      // preferences: {
      //   visibility: visibility.value,
      //   genres: selectedGenres.value
      // }
    })

    emit('save', response)  
    open.value = false
  }
  catch(err){
    console.error("Failed to update profile details", err)
    show(error.value, "error");
  }
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preferences-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preferences-section h3 {
  margin: 0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
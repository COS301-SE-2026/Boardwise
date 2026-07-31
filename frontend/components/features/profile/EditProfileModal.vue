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
import { useProfile } from '~/composables/useProfile'
import { useSnackBar } from '~/composables/useSnackbar';


const { updateProfile, isLoading, error } = useProfile();
const { show } = useSnackBar();

const open = defineModel()
const emit = defineEmits(['save'])

const props = defineProps({
  user : {
    type: Object,
    required : true
  }
})


const name = ref(props.user.fullName)
const username = ref(props.user.username)
const location = ref(props.user.location)
const bio = ref(props.user.preferences.genres.join('•'))

const handleSave = async () => {
  try{
    const response = await updateProfile({
      name : name.value,
      username : username.value,
      location : location.value
    })
    emit('save', response)  
    resetRefs()
  }
  catch(err){
    console.error("Failed to update profile details", err)
    show(error.value, "error");
  }
}

const resetRefs = () => {
    // clear everything
    open.value = false
    name.value = ""
    username.value = ""
    location.value = ""
    bio.value = ""
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
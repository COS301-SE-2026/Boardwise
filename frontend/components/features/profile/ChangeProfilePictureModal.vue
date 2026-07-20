<template>
    <BaseModal v-model="open">
        
        <div class="d-flex flex-column">
            <h2>Edit Profile Picture</h2>

            <BaseImage
                :src="user.profilePicture ?? '/images/avatar.jpg'"
                :alt="user.username + 'profile picture'"
                :width="250"
                :height="250"
                class="rounded-circle align-self-center mt-2"
            />

            <v-actions class="d-flex justify-space-between mt-10">
                <div class="d-flex align-center ga-3">
                    <BaseButton 
                        variant="primary" 
                        @click="triggerUpload"
                        :disabled="isLoading"
                    >
                        <v-icon start>mdi-upload</v-icon>
                        {{ isLoading ? "Uploading..." : "Upload Image" }}
                    </BaseButton>

                    <span style="font-size: var(--fs-small); color: var(--color-text-muted)">
                        {{ fileName || 'No file chosen' }}
                    </span>
                    <label for="profile-picture-upload" class="sr-only">Upload Profile Picture</label>
                    <input
                        id="profile-picture-upload"
                        ref="fileInput"
                        type="file"
                        accept="image/*"
                        style="display: none;"
                        @change="handleFileChange"
                    />
                </div>

                <BaseButton 
                    variant="secondary"
                    @click="open = false"
                >
                    Cancel
                </BaseButton>
            </v-actions>
        </div>

    </BaseModal>
</template>
<script setup>
import BaseImage from '~/components/ui/BaseImage.vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import BaseButton from '~/components/ui/BaseButton.vue';
import { useProfile } from '~/composables/useProfile';
import { useSnackBar } from '~/composables/useSnackbar';

const { updateProfilePicture, isLoading, error } = useProfile();
const { show } = useSnackBar();

const open = defineModel(false)
const emit = defineEmits(['save'])

const fileName  = ref('');
const fileInput = ref(null);


defineProps({
    user : {
        type: Object,
        required: true
    }
})

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = async (e) => {
    const chosenFile = e.target.files[0];
    if (!chosenFile)
        return

    // make request here
    try{
        const profilePictureUrl = await updateProfilePicture(chosenFile);
        emit('save', { profilePictureUrl });
        open.value = false;
    }
    catch(err){
        console.error("Falied to change profile picture", err)
        show(error.value, "error");
    }
    
}

</script>

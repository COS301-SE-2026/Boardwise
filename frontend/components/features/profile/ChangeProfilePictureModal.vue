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
                    <BaseButton variant="primary" @click="triggerUpload">
                        <v-icon start>mdi-upload</v-icon>
                        Upload Image
                    </BaseButton>

                    <span style="font-size: var(--fs-small); color: var(--color-text-muted)">
                        {{ fileName || 'No file chosen' }}
                    </span>

                    <label for="pfp-upload" class="sr-only">Upload Profile Picture</label>
                    <input
                        id="pfp-upload"
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
                    cancel
                </BaseButton>
            </v-actions>
        </div>

    </BaseModal>
</template>
<script setup>
import BaseImage from '~/components/ui/BaseImage.vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import BaseButton from '~/components/ui/BaseButton.vue';


const open = defineModel()
const emit = defineEmits(['save'])

const fileName  = ref('');
const fileInput = ref(null);
const file = ref(null);

defineProps({
    user : {
        type: Object,
        required: true
    }
})

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
    const chosenFile = e.target.files[0];
    if (!chosenFile)
        return

    // make request here

    emit('save', "sumn");
}

</script>

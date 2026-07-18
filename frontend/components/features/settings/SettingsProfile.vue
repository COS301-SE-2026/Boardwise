<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-4">
            <div>
                <h2 class="text-h5">
                    General
                </h2>
                <p class="text-body-2 text-medium-emphasis">
                    Update personal Information
                </p>
            </div>  

            <BaseInput 
                v-model="form.firstName" 
                label="First name" 
                placeholder="Change first name"
                />

            <BaseInput 
                v-model="form.lastName" 
                label="Last Name" 
                placeholder="Change last name"  
                />
            <BaseInput 
                v-model="form.username"  
                label="Username"
                placeholder="Change username"  
            />
            <BaseInput 
                v-model="form.email"     
                type="email" 
                label="Email"    
                placeholder="Change email" 
                />
            <BaseInput 
                v-model="form.bio"       
                type="textarea"
                label="Bio"  
                placeholder="Change bio"   
                />
                <div class="d-flex justify-end mt-2">
            <BaseButton @click="emit('save', { ...form})">
                Save changes
            </BaseButton>
            </div>  
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue';

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { getProfile } from '~/services/settingsService'

const form = ref(getProfile())

const emit = defineEmits(['save'])
</script>

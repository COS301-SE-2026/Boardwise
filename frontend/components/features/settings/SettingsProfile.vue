<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-4">
            <div>
                <h2 class="text-h5">
                    General
                </h2>
                <p class="text-body-2 text-medium-emphasis">
                    Your account information
                </p>
            </div>  

            <BaseInput 
                :model-value="profile.firstName" 
                label="First name" 
                disabled
                />

            <BaseInput 
                :model-value="profile.lastName" 
                label="Last Name" 
                disabled
                />
            <BaseInput 
                :model-value="profile.username"  
                label="Username"
                disabled
            />
            <BaseInput 
                :model-value="profile.email"     
                type="email" 
                label="Email"    
                disabled
                />
            <BaseInput 
                :model-value="profile.bio"       
                type="textarea"
                label="Bio"  
                disabled   
                />
        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import { getProfile } from '~/services/settingsService'

const profile = getProfile()
</script>

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

            <v-row>
                <v-col cols="12" sm="6">
            <BaseInput 
                :model-value="profile.firstName" 
                label="First name" 
                readonly
                />
</v-col></v-row>
        <v-row>
            <v-col cols="12" sm="6">
            <BaseInput 
                :model-value="profile.lastName" 
                label="Last Name" 
                readonly
                /></v-col></v-row>

                <v-row>
            <v-col cols="12" >
            <BaseInput 
                :model-value="profile.username"  
                label="Username"
                readonly
            /></v-col></v-row>
            <v-row>
            <v-col cols="12" >
            <BaseInput 
                :model-value="profile.email"     
                type="email" 
                label="Email"    
                readonly
                /></v-col></v-row>
            <v-row>
            <v-col cols="12" >
            <BaseTextArea
                        :model-value="profile.bio"
                        label="Bio"
                        :rows="3"
                        readonly  
                />
            </v-col></v-row>

                <v-alert
                type="info"
                variant="tonal"
                density="comfortable"
            >
                Profile information can be updated from your profile page.
            </v-alert>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue';
import { getProfile } from '~/services/settingsService'

const profile = getProfile()
</script>

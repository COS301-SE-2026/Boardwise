<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-5">

            <div>
                <h2 class="text-h5">
                    Notifications
                </h2>
                <p class="text-body-2 text-medium-emphasis">
                    Choose what activity you want to be alerted about.
                </p>
            </div> 
            <v-list>
            <v-list-item 
                v-for="item in items" 
                :key="item.key"
                >

                <div class="d-flex justify-space-between align-center w-100">
                    <div>
                        <div class="text-body-1 font-weight-medium">
                            {{ item.label }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis">
                            {{ item.description }}
                        </div>
                    </div>

                    <v-switch
                        v-model="item.enabled" 
                        color="primary"
                        hide-details   
                    />
                </div>
            </v-list-item>
            </v-list>   
               
            <div class="d-flex justify-end">
                <BaseButton @click="savePreferences">
                    Save Preferences
                </BaseButton>
            </div>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { getNotification } from '~/services/settingsService'


const emit = defineEmits(['save'])

const notificationSettings = getNotification()

const items = [
    { 
        key: 'event_rsvp',           
        label: 'Event RSVPs',          
        description: 'Someone joins or declines your event.',  
        enabled: notificationSettings.event_rsvp  
    },
    { 
        key: 'friend_request',       
        label: 'Friend Requests',      
        description: 'A user sends you a friend request.',     
        enabled: notificationSettings.friend_request  
    },
    { 
        key: 'marketplace_interest',           
        label: 'Marketplace Interests',          
        description: 'New items match your interests.',  
        enabled: notificationSettings.marketplace_interest  
    },
    { 
        key: 'vault_update',       
        label: 'Vault Updates',      
        description: 'A user updates their vault.',     
        enabled: notificationSettings.vault_update  
    },
    { 
        key: 'community_event',           
        label: 'Community Events',          
        description: 'New events in your community.',  
        enabled: notificationSettings.community_event  
    },
]

const savePreferences = () => {
    emit(
        'save',
        items.reduce((acc, item) => {
            acc[item.key] = item.enabled
            return acc
        },{})
    )
}
</script>

<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-5">
            <div>
                <h2 class="text-h5">
                    Privacy
                </h2>
                <p class="text-body-2 text-medium-emphasis">
                    Control who can see your profile and activity.
                </p>
            </div> 

           <v-select
                v-model="visibility"
                :items="visibilityOptions"
                label="Profile Visibility"
                item-title="label"
                item-value="value"
                variant="outlined"
                hide-details
           />

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
            <BaseButton @click="savePrivacy">
                Save Privacy Settings
            </BaseButton>
           </div>
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { getPrivacy } from '~/services/settingsService'

const emit = defineEmits(['save'])

const privacy = getPrivacy()

const visibility = ref(privacy.visibility)

const visibilityOptions = [
    {   label: 'Public', value:'public'},
    {   label: 'Community Members Only', value:'community'},
    {   label: 'Private', value:'private'},
]

const savePrivacy = () => {
    emit('save', {
        visibility: visibility.value,
        Settings: items.value.reduce((acc, item) => {
            acc[item.key] = item.enabled
            return acc
        }, {})
    })
}
const items = ref([
    {
        key: 'show_online_status', 
        label: 'Show Online Status', 
        description: 'Allow others to see when you are online.', 
        enabled: privacy.settings.show_online_status
    },
    {
        key: 'show_activity', 
        label: 'Show Activity', 
        description: 'Allow others to see your recent activity.', 
        enabled: privacy.settings.show_activity
    },
    {
        key: 'show_friends', 
        label: 'Show Friends List', 
        description: 'Allow others to see your friends list.', 
        enabled: privacy.settings.show_friends
    },
    {
        key: 'show_events', 
        label: 'Show Events', 
        description: 'Allow others to see the events you are attending.', 
        enabled: privacy.settings.show_events
    },
    {
        key: 'show_marketplace', 
        label: 'Show Marketplace', 
        description: 'Allow others to see your marketplace activity.', 
        enabled: privacy.settings.show_marketplace
    }
])
</script>

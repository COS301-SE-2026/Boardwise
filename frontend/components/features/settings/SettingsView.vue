<template>
        <div class="d-flex ga-6 align-start">
            <BaseCard class="pa-4" min-width="240">
                <h2 class="text-h6 mb-4">
                    Settings
                </h2>

                <v-list nav density="comfortable">
                <v-list-item 
                    v-for="section in sections" 
                    :key="section.id"
                    :active=" active === section.id"
                    rounded="lg"
                    @click="active = section.id"
                    >
                    <v-list-item-title>
                        {{ section.label }}
                    </v-list-item-title>
                </v-list-item>
            </v-list>
        </BaseCard>

        <div class="flex-grow-1">
            <SettingsProfile v-if="active === 'profile'"/>
            <SettingsAppearance v-else-if="active === 'appearance'"/>
            <SettingsPrivacy v-else-if="active === 'privacy'"/>
            <SettingsNotifications v-else-if="active === 'notifications'"/>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

import BaseCard from '~/components/ui/BaseCard.vue'
import SettingsProfile from './SettingsProfile.vue'
import SettingsAppearance from './SettingsAppearance.vue'
import SettingsPrivacy from './SettingsPrivacy.vue'
import SettingsNotifications from './SettingsNotifications.vue'

const active = ref('profile')

const sections = [
    { id: 'profile', label: 'Profile' },
    { id: 'appearance', label: 'Appearance' },
    { id: 'privacy', label: 'Privacy' },
    { id: 'notifications', label: 'Notifications' }
]
</script>

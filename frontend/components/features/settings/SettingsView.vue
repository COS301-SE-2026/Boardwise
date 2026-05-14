<template>
        <div class="settings">
           
            <nav class="settings__nav">
                <h2>Settings</h2>
                <button 
                    v-for="section in sections" :key="section.id"
                    :class="{ active: active === section.id}"
                    @click="active = section.id">
                    {{ section.label }}
                </button>
            </nav>

            <main class="settings__content">
                <SettingsProfile v-if="active === 'profile'"  />
                <SettingsAppearance v-if="active === 'appearance'" />
                <SettingsPrivacy v-if="active === 'privacy'"  />
                <SettingsNotifications v-if="active === 'notifications'"  />
            </main>

        </div>
</template>

<script setup>
import { ref } from 'vue';
import SettingsProfile from './SettingsProfile.vue';
import SettingsAppearance from './SettingsAppearance.vue';
import SettingsPrivacy from './SettingsPrivacy.vue';
import SettingsNotifications from './SettingsNotifications.vue';    

const active = ref('profile');

const sections = [
    { id: 'profile', label: 'Profile' },
    { id: 'appearance', label: 'Appearance' },
    { id: 'privacy', label: 'Privacy' },
    { id: 'notifications', label: 'Notifications' }
]
</script>

<style scoped>
    .settings {
    display: flex;
    gap: 2rem;
}

.settings__nav {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 160px;
}

.settings__nav h2 {
    margin-bottom: 12px;
}

.settings__nav button{
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 160px;
}

.settings__row {
    display: flex;
    gap: 10px;
}

.settings__nav button {
    text-align: left;
    padding: 8px 12px;
    border-radius: 6px;
    background: none;
    border: none;
    cursor: pointer;
    color: #444;
}

.settings__nav button.active {
    background: #f0f0f0;
    font-weight: 500;
    color: #111;
}

settings__content {
    flex: 1;
}
</style>
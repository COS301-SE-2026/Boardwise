<template>
  <section aria-labelledby="settings-page-title">
    <div class="mb-6">
      

      <p class="text-body-1 text-medium-emphasis mb-0">
        From your account, appearance, privacy and notification preferences.
      </p>
    </div>

    <v-row>
      <v-col
        cols="12"
        md="4"
        lg="3"
      >
        <BaseCard class="pa-3">
          <nav aria-label="Settings sections">
            <v-list
              nav
              density="comfortable"
              class="bg-transparent"
            >
              <v-list-item
                v-for="section in sections"
                :key="section.id"
                :active="active === section.id"
                :prepend-icon="section.icon"
                rounded="lg"
                :aria-current="
                  active === section.id
                    ? 'page'
                    : undefined
                "
                @click="active = section.id"
              >
                <v-list-item-title>
                  {{ section.label }}
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </nav>
        </BaseCard>
      </v-col>

      <v-col
        cols="12"
        md="8"
        lg="9"
      >
        <component
          :is="activeComponent"
          @save="handleSave"
        />
      </v-col>
    </v-row>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'

import SettingsProfile from './SettingsProfile.vue'
import SettingsAppearance from './SettingsAppearance.vue'
import SettingsPrivacy from './SettingsPrivacy.vue'
import SettingsNotifications from './SettingsNotifications.vue'

const emit = defineEmits(['save'])

const active = ref('profile')

const sections = [
  {
    id: 'profile',
    label: 'Account',
    icon: 'mdi-account-outline'
  },
  {
    id: 'appearance',
    label: 'Appearance',
    icon: 'mdi-palette-outline'
  },
  {
    id: 'privacy',
    label: 'Privacy',
    icon: 'mdi-shield-lock-outline'
  },
  {
    id: 'notifications',
    label: 'Notifications',
    icon: 'mdi-bell-outline'
  }
]

const componentMap = {
  profile: SettingsProfile,
  appearance: SettingsAppearance,
  privacy: SettingsPrivacy,
  notifications: SettingsNotifications
}

const activeComponent = computed(() => {
  return componentMap[active.value] ?? SettingsProfile
})

const handleSave = (data) => {
  emit('save', {
    section: active.value,
    data
  })
}
</script>
<template>
  <BaseCard class="pa-6">
    <div class="d-flex flex-column ga-5">
      <header>
        <h2 class="text-h5 font-weight-bold mb-1">
          Notifications
        </h2>

        <p class="text-body-2 text-medium-emphasis mb-0">
          Choose which Boardwise activity you want to hear about.
        </p>
      </header>

      <v-list class="bg-transparent pa-0">
        <SettingsPreferenceRow
          v-for="item in items"
          :id="`notification-${item.key}`"
          :key="item.key"
          v-model="item.enabled"
          :label="item.label"
          :description="item.description"
        />
      </v-list>

      <v-divider />

      <div class="d-flex justify-end">
        <BaseButton
          :disabled="!hasChanges"
          @click="savePreferences"
        >
          Save Preferences
        </BaseButton>
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed, ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import SettingsPreferenceRow from './SettingsPreferenceRow.vue'

import { getNotification } from '~/services/settingsService'

const emit = defineEmits(['save'])

const notificationSettings = getNotification()

const items = ref([
  {
    key: 'event_rsvp',
    label: 'Event RSVPs',
    description: 'Someone joins or declines your event.',
    enabled: notificationSettings.event_rsvp
  },
  {
    key: 'friend_request',
    label: 'Friend Requests',
    description: 'Someone sends you a friend request.',
    enabled: notificationSettings.friend_request
  },
  {
    key: 'marketplace_interest',
    label: 'Marketplace Interests',
    description: 'New marketplace items match your interests.',
    enabled: notificationSettings.marketplace_interest
  },
  {
    key: 'vault_update',
    label: 'Vault Updates',
    description: 'Content in your Vault is updated.',
    enabled: notificationSettings.vault_update
  },
  {
    key: 'community_event',
    label: 'Community Events',
    description: 'A new event is created in one of your communities.',
    enabled: notificationSettings.community_event
  }
])

const getValues = () => {
  return items.value.reduce((result, item) => {
    result[item.key] = item.enabled
    return result
  }, {})
}

const savedValues = ref(
  JSON.stringify(getValues())
)

const hasChanges = computed(() => {
  return JSON.stringify(getValues()) !== savedValues.value
})

const savePreferences = () => {
  const preferences = getValues()

  emit('save', preferences)

  savedValues.value = JSON.stringify(preferences)
}
</script>
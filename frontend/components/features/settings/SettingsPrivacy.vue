<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-5">
            <header>
                <h2 class="text-h5 font-weight-bold mb-1">
                    Privacy
                </h2>

                <p class="text-body-2 text-medium-emphasis mb-0">
                    Control who can see your profile and activity.
                </p>
            </header>


            <div>
                <h3 class="text-subtitle-1 font-weight-bold mb-2">
                    Profile visibility
                </h3>

                <v-select
                    v-model="visibility"
                    :items="visibilityOptions"
                    label="Who can see your profile?"
                    item-title="label"
                    item-value="value"
                    variant="outlined"
                    rounded="lg"
                    hide-details="auto"
                />
            </div>

            <v-divider />


            <div>
                <h3 class="text-subtitle-1 font-weight-bold mb-2">
                    Activity visibility
                </h3>

                <p class="text-body-2 text-medium-emphasis mb-2">
                    Choose which parts of your activity other members can see.
                </p>

                <v-list class="bg-transparent pa-0">
                    <SettingsPreferenceRow
                        v-for="item in items"
                        :id="`privacy-${item.key}`"
                        :key="item.key"
                        v-model="item.enabled"
                        :label="item.label"
                        :description="item.description"
                    />
                </v-list>
            </div>

            <v-divider />


            <div class="d-flex align-center justify-space-between flex-wrap ga-3">
                <div
                    class="privacy-save-status"
                    aria-live="polite"
                    aria-atomic="true"
                >
                    <v-chip
                        v-if="hasChanges"
                        color="warning"
                        variant="tonal"
                        prepend-icon="mdi-pencil-outline"
                    >
                        Unsaved changes
                    </v-chip>

                    <v-chip
                        v-else-if="saveStatus === 'saved'"
                        color="success"
                        variant="tonal"
                        prepend-icon="mdi-check-circle-outline"
                    >
                        Changes saved
                    </v-chip>

                    <span
                        v-else
                        class="text-body-2 text-medium-emphasis"
                    >
                        Your privacy settings are up to date.
                    </span>
                </div>

                <BaseButton
                    :disabled="!hasChanges"
                    @click="savePrivacy"
                >
                    Save Privacy Settings
                </BaseButton>
            </div>
        </div>
    </BaseCard>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import SettingsPreferenceRow from './SettingsPreferenceRow.vue'

import { getPrivacy } from '~/services/settingsService'

const emit = defineEmits(['save'])

const privacy = getPrivacy()

const visibility = ref(privacy.visibility)

const visibilityOptions = [
    {
        label: 'Public',
        value: 'public'
    },
    {
        label: 'Community Members Only',
        value: 'community'
    },
    {
        label: 'Private',
        value: 'private'
    }
]

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
        label: 'Show Marketplace Activity',
        description: 'Allow others to see your marketplace activity.',
        enabled: privacy.settings.show_marketplace
    }
])

const getSettings = () => {
    return items.value.reduce((result, item) => {
        result[item.key] = item.enabled
        return result
    }, {})
}

const getCurrentState = () => ({
    visibility: visibility.value,
    settings: getSettings()
})

const savedState = ref(
    JSON.stringify(getCurrentState())
)

const saveStatus = ref('')

const hasChanges = computed(() => {
    return JSON.stringify(getCurrentState()) !== savedState.value
})

watch(
    [visibility, items],
    () => {
        if (hasChanges.value) {
            saveStatus.value = ''
        }
    },
    {
        deep: true
    }
)

const savePrivacy = () => {
    if (!hasChanges.value) return

    const updatedPrivacy = getCurrentState()

    emit('save', updatedPrivacy)

    savedState.value = JSON.stringify(updatedPrivacy)
    saveStatus.value = 'saved'
}
</script>
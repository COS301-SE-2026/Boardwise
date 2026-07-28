<template>
    <BaseCard class="pa-6">
        <div class="d-flex flex-column ga-5">
            <div>
                <h2 class="text-h5">
                    Appearance
                </h2>
                <p class="text-body-2 text-medium-emphasis">
                    Customise how Boardwise looks for you
                </p>
            </div>  

            <v-list>
            <v-list-item 
                v-for="theme in themes" 
                :key="theme.value"
                >

                <div class="d-flex justify-space-between align-center w-100">
                    <div>
                        <div class="text-body-1 font-weight-medium">
                            {{ theme.label }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis">
                            {{ theme.description }}
                        </div>
                    </div>

                    <v-switch
                        v-model="theme.enabled" 
                        color="primary"
                        hide-details
                        @update:model-value="toggleTheme(theme.value)"   
                    />
                </div>
            </v-list-item>
            </v-list>   

            <div class="d-flex justify-end mt-2">
            <BaseButton @click="saveAppearance">
                Apply
            </BaseButton>
        </div>
    </div>
</BaseCard>
</template>

<script setup>
import { ref } from 'vue';

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { getAppearance } from '~/services/settingsService';

const emit = defineEmits(['save'])

const currentTheme = getAppearance().theme

const themes = ref([
    {          
        label: 'Light Mode',          
        value: 'light',
        description: 'Use the light Boardwise theme.',
        enabled: currentTheme === 'light'
    },
    {      
        label: 'Dark Mode',
        value: 'dark',      
        description: 'Use the dark Boardwise theme.',
        enabled: currentTheme === 'dark'
    },
    {            
        label: 'System Default', 
        value: 'system',         
        description: 'Follow your device theme settings.',    
        enabled: currentTheme === 'system'
    }
])

const toggleTheme = (selectedTheme) => {
    themes.value.forEach(theme => {
        theme.enabled = theme.value === selectedTheme
    })
}
const saveAppearance = () => {
    const selectedTheme = themes.value.find(
        theme => theme.enabled
    )
    emit( 'save', {
        theme: selectedTheme.value
    })
}
</script>

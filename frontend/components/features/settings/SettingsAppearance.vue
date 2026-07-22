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
                :key="theme.key"
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
                        :model-value="appearance === theme.value" 
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

const appearance = ref(getAppearance().theme)

const themes = [
    {          
        label: 'Light Mode',          
        value: 'light',
        description: 'Use the light Boardwise theme.',
    },
    {      
        label: 'Dark Mode',
        value: 'dark',      
        description: 'Use the dark Boardwise theme.',
    },
    {            
        label: 'System Default', 
        value: 'system',         
        description: 'Follow your device theme settings.',    
    }
]

const saveAppearance = () => {
    emit( 'save', {
        theme: appearance.value
    })
}
</script>

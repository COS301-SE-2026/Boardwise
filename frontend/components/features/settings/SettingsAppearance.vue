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

            <div class="d-flex flex-column ga-3">
                <span class="text-subtitle-2">
                    Themes
                </span>

                <v-btn-toggle 
                    v-model="appearance"
                    color="primary"
                    mandatory
                >
                    <v-btn
                        v-for="theme in themes" 
                        :key="theme.value"
                        :value="theme.value"
                    >
                    {{ theme.label }}
                </v-btn>
            </v-btn-toggle>
        </div>

            <div class="d-flex justify-end mt-2">
            <BaseButton @click="emit('save', {  theme: appearance})">
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

const emit = defineEmits(['save']);

const appearance = ref(getAppearance().theme)

const themes = [
    { label: 'Light', value: 'light' },
    { label: 'Dark', value: 'dark' },
    { label: 'System', value: 'system' },
]
</script>

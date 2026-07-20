<template>
    <v-container class="py-16">

        <v-row justify="center">
            <v-col
                v-for="stat in stats"
                :key="stat.label"
                cols="6"
                md="3"
            >
                <BaseCard class="pa-6 text-center fill-height">
                    <div class="text-h3 font-weight-bold text-primary">
                        {{ stat.value }}
                    </div>
                    <div class="text-body-1 mt-2">
                        {{ stat.label }}
                    </div>
                </BaseCard>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

const stats = [
    {
        value: '200+',
        label: 'Board games'
    },
    {
        value: '100+',
        label: 'Community members'
    },
    {
        value: '50+',
        label: 'Events hosted'
    },
    {
        value: '24/7',
        label: 'Platform access'
    }
]
</script>
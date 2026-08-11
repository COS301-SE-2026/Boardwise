<template>
    <v-container class="pb-10">

         <h3 class="mb-6">
            Frequently Asked Questions
         </h3>

        <v-expansion-panels class="mt-6" variant="accordion">

            <v-expansion-panel
                v-for="item in faqs"
                :key="item.question"
            >
                <v-expansion-panel-title>
                    {{ item.question }}
                </v-expansion-panel-title>

                <v-expansion-panel-text >
                    <span class="text-primary">
                    {{ item.answer }}
                    </span>
                </v-expansion-panel-text>
            </v-expansion-panel>

        </v-expansion-panels>

    </v-container>
</template>

<script setup>
import { faqs } from '~/services/mockData/help'
</script>
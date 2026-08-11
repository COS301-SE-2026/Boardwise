<template>
    <v-container class="pb-10">
        <SectionTitle
            title="Quick Start Guides"
            subtitle="Jump straight to the feature you need help with."
        />

        <v-row class="mt-4">
            <v-col
                v-for="tutorial in tutorials"
                :key="tutorial.title"
                cols="12"
                md="6"
            >
                <BaseCard class="d-flex flex-column fill-height pa-6">
                    <h3>{{ tutorial.title }}</h3>

                    <p class="text-medium-emphasis mt-2">
                        {{ tutorial.description }}
                    </p>

                    <ol class="mt-4 pl-5">
                        <li
                            v-for="step in tutorial.steps"
                            :key="step"
                            class="mb-2"
                        >
                            {{ step }}
                        </li>
                    </ol>

                    <div class="mt-auto pt-6">
                    <BaseButton
                        class="mt-6"
                        @click="router.push(tutorial.route)"
                    >
                        {{ tutorial.button }}
                    </BaseButton>
                    </div>
                </BaseCard>
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import { tutorials } from '~/services/mockData/help'

const router = useRouter()
</script>
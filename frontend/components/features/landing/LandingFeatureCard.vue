<template>
    <BaseCard class="pa-6 h-100">
        <v-img
            :src="feature.image"
            height="220"
            contain
            class="mb-4"
        />

        <v-chip size="small" color="primary" class="mb-3">
            {{ feature.tag }}
        </v-chip>
        <h3 class="text-h6 font-weight-bold">
            {{ feature.title }}
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-2">
            {{ feature.description }}
        </p>

        <v-list>
            <v-list-item 
                v-for="highlight in feature.highlights"
                :key="highlight"
                prepend-icon="mdi-check-circle"
            >
                <v-list-item-title>
                    {{ highlight }}
                </v-list-item-title>
            </v-list-item>
        </v-list>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
    feature: {
        type: Object,
        required: true
    }
})
</script>

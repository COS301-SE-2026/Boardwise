<template>
    <BaseCard class="pa-6 h-100">
        <v-icon
            :src="game.imageUrl"
            height="220"
            contain
            class="rounded-lg mb-4"
        />

        <v-chip
            size="small"
            color="primary"
            class="mb-3"
        >
            {{ game.genres?.[0] || 'Board game' }}
        </v-chip>

        <h3 class="text-h5 font-weight-bold mb-3">
            {{ game.title }}
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-2">
            {{ game.description }}
        </p>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
    game: {
        type: Object,
        required: true
    }
})
</script>

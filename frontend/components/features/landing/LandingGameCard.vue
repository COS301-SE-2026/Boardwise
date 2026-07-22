<template>
    <BaseCard class="pa-6 text-center h-100 cursor-pointer d-flex flex-column justify-center"
    @click="navigateTo(game)">

        <v-icon
            :icon="game.icon"
            size="64"
            color="secondary"
            class="mb-4"
        />

        <h3>
            {{ game.title }}
        </h3>

        <h3>
            {{ game.description }}
        </h3>

    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

const router = useRouter()

const props = defineProps({
    game: {
        type: Object,
        required: true
    }
})

const navigateTo = (game) => {
    router.push(game.route)
}
</script>

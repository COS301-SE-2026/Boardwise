<template>
    <BaseGrid data-test="player-grid">
        <PlayerCard
            v-for="player in players"
            :key="player.id"
            :player="player"
            :variant="variant"
            @friend-request="$emit('friend-request', $event)"
            @message="$emit('message', $event)"
            @unfriend="$emit('unfriend', $event)"
        />
    </BaseGrid>
</template>

<script setup>
import PlayerCard from './PlayerCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

defineProps({
    players: { type: Array, default: () => []},
    variant: {
        type: String,
        default: 'discover',
        validaor: (v) => ['discover', 'friend'].includes(v)
    }
})

defineEmits(['friend-request', 'message', 'unfriend'])
</script>
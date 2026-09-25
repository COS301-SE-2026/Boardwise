<template>
    <BaseGrid data-test="people-grid">
        <PlayerCard
            v-for="person in people"
            :key="person.id"
            :person="person"
            :variant="variant"
            @friend-request="$emit('friend-request', $event)"
            @message="$emit('message', $event)"
            @unfriend="$emit('unfriend', $event)"
        />
    </BaseGrid>
</template>

<script setup>
import PlayerCard from '../PlayerCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

defineProps({
    people: { type: Array, default: () => []},
    variant: {
        type: String,
        default: 'discover',
        validaor: (v) => ['discover', 'friend'].includes(v)
    }
})

defineEmits(['friend-request', 'message', 'unfriend'])
</script>
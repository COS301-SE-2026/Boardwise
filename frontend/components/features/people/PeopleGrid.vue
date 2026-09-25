<template>
    <BaseGrid data-test="people-grid">
        <PersonCard
            v-for="person in people"
            :key="person.id"
            :person="person"
            :variant="variant"
            @add-friend="$emit('add-friend', $event)"
            @message="$emit('message', $event)"
            @unfriend="$emit('unfriend', $event)"
        />
    </BaseGrid>
</template>

<script setup>
import PersonCard from '../PersonCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

defineProps({
    people: { type: Array, default: () => []},
    variant: {
        type: String,
        default: 'discover',
        validator: (v) => ['discover', 'friend'].includes(v)
    }
})

defineEmits(['add-friend', 'message', 'unfriend'])
</script>
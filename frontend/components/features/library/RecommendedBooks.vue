<template> 
    <div class="mt-10">

        <SectionTitle 
            title="Recommended For You"
        />

        <div class="d-flex ga-4 overflow-x-auto pb-3 mt-4">
        <RulebookCard
            v-for="rulebook in rulebooks"
            :key="rulebook.id"
            :rulebook="rulebook"
            style="min-width: 180px; max-width: 180px;"
            @click="$emit('select', $event)"
        />
        </div>
    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import RulebookCard from './RulebookCard.vue'

defineProps({
    rulebooks: {
        type: Array,
        default: () => []
    }
})

defineEmits(['select'])
</script>
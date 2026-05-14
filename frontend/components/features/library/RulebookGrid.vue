<template> 
    <div class="grid">

        <RulebookCard 
            v-for="rulebook in rulebooks"
            :key="rulebook.id"

            :id="rulebook.id"
            :title="rulebook.title"
            :image="rulebook.image"
            :price="rulebook.category"
        />
    </div>
</template>

<script setup>
import RulebookCard from './RulebookCard.vue'

const rulebooks = [
    {
        id: 1,
        title: 'Catan Rules',
        image: '/games/catan.jpg',
        category: 'Strategy'
    },

    {
        id:2,
        title: 'Wingspan Guide',
        image: '/games/wingspan.jpg',
        category: 'Engine Builder'
    }
]
</script>

<style scoped>
.grid {
    display: grid;

    grid-template-columns:
        repeat(auto-fit, minmax(220px, 1fr)); //auto wraps to the device (desktop and mobile)

    gap: 20px;
}
</style>
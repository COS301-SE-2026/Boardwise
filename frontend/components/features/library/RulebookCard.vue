<template> 
    <BaseCard class="card" @click="openBook"> 

        <div class="image-wrapper">
            <img :src="rulebook.image" :alt="rulebook.title" />
        </div>
    
        <div class="content">

            <h3>{{ rulebook.title }}</h3>
            <p>{{ rulebook.category }}</p>
        </div>
        
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    rulebook: Object
})

const openBook = () => {
    navigateTo('/library/${props.rulebook.id}')
}
</script>

<style scoped>
.card {
    display: flex;
    flex-direction: column;
    gap: 12px;
    cursor: pointer;
    transition: 0.2s;
    min-width: 180px;
    max-width: 180px;
}

.card:hover {
    transform: translate(-4px);
}

.image-wrapper {
    height: 100%;
    width: 100%;
    object-fit: contain;
}

img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.content {
    padding-top: 12px;
}
</style>
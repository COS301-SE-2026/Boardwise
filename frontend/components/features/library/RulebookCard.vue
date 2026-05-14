<template> 
    <BaseCard> 

        <div class="card" @click="openBookDetails">

            <div class="image-container">
                <img :src="image" :alt="title" />
            </div>
        
            <div class="content">

                <h3>{{ title }}</h3>
                <p>{{ category }}</p>
            </div>
        </div>
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    id: Number,
    title: String,
    image: String,
    category: String
})

const router = useRouter()

const openBook = () => {
    router.push('/library/${props.id}')
}
</script>

<style scoped>
.card {
    display: flex;
    flex-direction: column;
    gap: 12px;
    cursor: pointer;
}

.image-container {
    height: 240px;
    display: flex;
    justify-content: center;
    align-items: center;

    background: #f5f5f5;
    
    border-radius: 12px;
    overflow: hidden;
}

img {
    width: 100%;
    height: 100%;

    object-fit: contain;
}

.content {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

h3 {
    margin: 0;
}

p {
    color: #777;
}
</style>
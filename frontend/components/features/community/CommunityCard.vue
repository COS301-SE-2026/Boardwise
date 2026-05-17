<template> 
    <BaseCard> 

        <div class="community-item" @click="navigateToCommunity">

            <div class="image-container">
                <img :src="community.image" :alt="community.name" />
            </div>>
        
            <div class="content">

                <h3>{{ community.name }}</h3>
                <BaseBadge>{{ community.type }}</BaseBadge>
                <p class="description">{{ community.description }}</p>
            </div>
 
            <BaseButton @click="router.push(`/community/${community.id}/chat`)">
                View
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useRouter } from 'vue-router'

const props = defineProps({
    community: {
        type: Object, required: true 
    }
})

defineEmits(['join'])

const router = useRouter()

const navigateToCommunity = () => {
    router.push(`/community/${props.community.id}`)
}
</script>

<style scoped>
.community-item {
    display: flex;
    flex-direction: column;
    gap: 12px;
}
 
.image-container {
    height: 180px;
    border-radius: 12px;
    overflow: hidden;
    background: #f5f5f5;
}
 
.image-container img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
 
.content {
    display: flex;
    flex-direction: column;
    gap: 6px;
    flex: 1;
}
 
h3 {
    margin: 0;
}
 
.description {
    font-size: 0.85rem;
    color: #777;
    line-height: 1.5;
    margin: 0;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
    }
</style>
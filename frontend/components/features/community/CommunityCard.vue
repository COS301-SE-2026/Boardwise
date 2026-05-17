<template> 
    <BaseCard> 

        <div class="community-item" @click="navigateToCommunity">

            <div class="image-container">
                <BaseAvatar :label="image" :name="community.name" size="xl"  />
            </div>
        
            <div class="content">

                <h3 class="text-bold">{{ community.name }}</h3>
                <BaseBadge :src="community.category" || type="secondary"/>
            </div>
        </div>
    </BaseCard>
</template>

<script setup> 
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
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

.community-card__header {
    display: flex;
    gap: 12px;
    align-items: center;
}
.community-card__description {
    font-size: 0.9rem;
  color: var(--color-text-muted);
}
.community-card__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
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
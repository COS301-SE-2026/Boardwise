<template> 
    <div class="info">

        <h1>{{ title }}</h1>

        <p class="category">
            {{ category }}
        </p>

        <p class="description">
            {{ description }}
        </p>

        <BaseButton @click="readBook">
            Read Rulebook
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const props = ({
        id: Number,
        title: String,
        category: String,
        description: String
})

const router = useRouter()

const readBook = () => {
    router.push('/library/${props.id}/read')
}
</script>

<style scoped>
.info {
    display: flex;
    flex-direction: column;

    gap: 16px;
}

.category {
    color: #7B2CBF;
    font-weight: bold;
}

.description {
    line-height: 1.6;
    color: #555;
}
</style>
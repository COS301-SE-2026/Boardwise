<template> 
    <div class="info">

        <h1>{{ rulebook.title }}</h1>

        <p class="category">
            {{ rulebook.category }}
        </p>

        <p class="description">
            {{ rulebook.description }}
        </p>

        <BaseButton @click="readBook">
            Read Rulebook
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps ({
        rulebook: {
            type: Object,
            required: true
        }
})

const router = useRouter()

const readBook = () => {
    router.push('/rulebook/${props.rulebook.id}/read')
}
</script>

<style scoped>
.info {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.category {
    color: #6C3BFF;
    font-weight: bold;
}

.description {
    line-height: 1.6;
    color: #555;
}
</style>
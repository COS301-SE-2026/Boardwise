<template>
  <div class="d-flex flex-column ga-4">
    <h1 class="text-h4 font-weight-bold">
        {{ rulebook.title }}
    </h1>
    
    <p class="text-primary font-weight-bold mb-0">
        {{ rulebook.edition }}
    </p>
    
    <p class="text-medium-emphasis mb-0" style="line-height: 1.6;">
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
    router.push(`/rulebook/${props.rulebook.id}/read`)
}
</script>
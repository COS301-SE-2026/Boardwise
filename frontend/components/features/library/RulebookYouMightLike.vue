<template>
    <div v-if="suggestions.length">
        <h3 class="text-subtitle-1 font-weight-bold mb-3">
            You Might Also Like
        </h3>

        <div class="d-flex flex-column ga-2">
            <div 
                v-for="rulebook in suggestions"
                :key="rulebook.id"
                class="d-flex flex-column ga-2 pa-2 cursor-pointer"
                style="border-radius: 12px;"
                @click="$emit('select', rulebook)"
            >
                <BaseImage
                    :src="rulebook.image"
                    :alt="rulebook.title"
                    width="48px"
                    height="48px"
                    fit="cover"
                    style="border-radius: 8px;" />
                
                <span class="text-body-2">
                    {{ rulebook.title }}
                </span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    rulebook: {
        type: Array,
        default: () => []
    },
    currentId: {
        type: String,
        default: ''
    }
})

defineEmits(['select'])

const suggestions = computed(() => {
    props.rulebooks
        .filter(rb => rb.id !== props.currentId)
        .slice(0, 3)
})

</script>
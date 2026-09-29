<template> 
    <div class="base-pagination">
        <BaseButton 
            variant="ghost"
            size="sm"
            class="base-pagination__arrow"
            data-test="pagination-prev"
            :disabled="modelValue <= 1"
            aria-label="Previous page"
            @click="goTo(modelValue - 1)"
        >
            <v-icon size="18">mdi-chevron-left</v-icon>  
        </BaseButton>

            <ul class="base-pagination__list">
                <li v-for="(page, i) in pages" :key="`${page}-${i}`">
                    <span v-if="page === ELLIPSIS" class="base-pagination__ellipsis">...</span>

                    <BaseButton 
                        v-else
                        :variant="page === modelValue ? 'primary' : 'ghost'"
                        size="sm"
                        class="base-pagination__page"
                        :data-test="`pagination-page-${page}`"
                        :aria-current="page === modelValue ? 'page' : undefined"
                        :aria-label="`Page ${page}`"
                        @click="goTo(page)"
                    >
                        {{ page }}
                    </BaseButton>
                </li>
            </ul>

        <BaseButton 
            variant="ghost"
            size="sm"
            class="base-pagination__arrow"
            data-test="pagination-next"
            :disabled="modelValue >= totalPages"
            aria-label="Next page"
            @click="goTo(modelValue + 1)"
        >
            <v-icon size="18">mdi-chevron-right</v-icon>  
        </BaseButton>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import BaseButton from './BaseButton.vue';

const ELLIPSIS = '...'

const props = defineProps({
    modelValue: { type: Number, required: true },
    totalPages: { type: Number, required: true },
    siblingCount: { type: Number, default: 1 }
})

const emit = defineEmits(['update:modelValue'])

const goTo = (page) => {
    if (page < 1 || page > props.totalPages || page === props.modelValue) return
    emit('update:modelValue', page)
}

const pages = computed(() => {
    const total = props.totalPages
    const current = props.modelValue
    const sibling = props.siblingCount

    const totalVisible = sibling * 2 + 5

    if(total <= totalVisible) {
        return Array.from({ length: total }, (_, i) => i + 1)
    }

    const leftIndex = Math.max(current - sibling, 1)
    const rightIndex = Math.min(current + sibling, total)

    const showLeftEllipsis = leftIndex > 2
    const showRightEllipsis = rightIndex < total - 1

    const result = [1]

    if(showLeftEllipsis) result.push(ELLIPSIS)
    for(let p = leftIndex; p <= rightIndex; p++) {
        if(p !== 1 && p !== total) result.push(p)
    }

    if(showRightEllipsis) result.push(ELLIPSIS)

    result.push(total)
    return result
})
</script>
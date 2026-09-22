<template>
    <div class="rag-citation" data-test="rag-citation" @click="handleClick">
        <v-icon size="14" class="mr-1">mdi-book-open-variant</v-icon>
        <!-- <span class="rag-citation-text">{{ citation.content  }}</span> -->
        <span class="rag-citation-text">Section {{ citation.index + 1 }}</span>
    </div>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import type { Citation } from '~/services/ragService';

const props = defineProps<{
    citation: Citation
}>() 

const jumpToSection = inject<(index: number) => void>('jumpToSection')

const handleClick = () => {
    if(jumpToSection && typeof props.citation.index === 'number'){
        jumpToSection(props.citation.index)
    }
}
</script>
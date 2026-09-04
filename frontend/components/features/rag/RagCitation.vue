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

<style scoped>
.rag-citation {
    font-size: var(--fs-body-sm, .8rem);
    color: var(--color-text-muted);
    margin-top: var(--space-2, 8px);
    display: flex;
    align-items: center;

    cursor: pointer;
    transition: color 0.2s ease, background-color 0.2s ease;
    padding: 2px 6px;
    border-radius: 4px;
    margin-left: -6px;
    width: fit-content;
}

.rag-citation:hover{
    color: rgb(var(--v-theme-primary));
    background-color: rgba(var(--v-theme-primary), 0.1);
}
</style>
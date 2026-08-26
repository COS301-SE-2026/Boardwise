<template>
    <div class="rag-citation" data-test="rag-citation">
        <v-icon size="14" class="mr-1">mdi-book-open-variant</v-icon>
        Source: chunk {{  citation.index }}
    </div>
</template>

<script setup lang="ts">
import type { Citation } from '~/services/ragService';

defineProps<{
    citation: Citation
}>() 
</script>

<style scoped>
.rag-citation {
    font-size: var(--fs-body-sm, .8rem);
    color: var(--color-text-muted);
    margin-top: var(--space-2, 8px);
    display: flex;
    align-items: center;
}
</style>
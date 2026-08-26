<template>
    <div class="section-header d-flex justify-space-between align-center">
        <h2 class="section-header-title">{{  title }}</h2>

        <BaseButton
            v-if="showSeeAll"
            variant="text"
            size="small"
            @click="$emit('see-all')"
        >
            All
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';

defineProps({
    title: { type: String, required: true},
    showSeeAll: { type: Boolean, default: false }
})

defineEmits(['see-all'])
</script>

<style scoped>
.section-header {
    margin-bottom: var(--space-3);
}

.section-header-title {
    font-weight: var(--fw-bold);
}

</style>
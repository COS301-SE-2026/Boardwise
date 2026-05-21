<template> 
    <div class="base-grid">
        <slot />
    </div>
</template>

<script setup>
const props = defineProps({
    cols: {
        type: String,
        default: '280px'
    },
    gap: {
        type: String,
        default: '20px'
    }
})
</script>

<style scoped> 
.base-grid {
    display: grid;
    grid-template-columns: v-bind("'repeat(auto-fit, minmax(' + cols + ', 1fr))'");
    gap: v-bind(gap);
}
</style>
<template> 
    <div class="d-flex flex-wrap ga-6 w-100">
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

const gridColumns = computed(() => 'repeat(auto-fill, minmax(' + props.cols + ', 1fr))')
</script>

<!-- <style scoped> 
.base-grid {
    /* display: grid;
    grid-template-columns: v-bind("'repeat(auto-fit, minmax(' + cols + ', 1fr))'");
    gap: v-bind(gap); */
}
</style> -->
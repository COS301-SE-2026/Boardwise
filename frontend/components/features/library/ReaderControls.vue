<template> 
    <div class="controls">

        <BaseButton>
            A+
        </BaseButton>

        <BaseButton>
            A-
        </BaseButton>

        <BaseButton>
            Dark Mode
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
</script>

<style scoped>
.controls {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;
    flex-wrap: wrap;
}
</style>
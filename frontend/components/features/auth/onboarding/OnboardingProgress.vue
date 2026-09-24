<template>
    <div class="onboarding-progress">
        <span class="onboarding-progress__label">Step {{  current  }} of {{  total  }}</span>

        <progress class="onboarding-progress__track" :value="current" :max="total">
            {{ Math.round((current / total) * 100)}}%
        </progress>
    </div>
</template>

<script setup>
defineProps({
    current: { type: Number, required: true },
    total: { type: Number, required: true }
})
</script>
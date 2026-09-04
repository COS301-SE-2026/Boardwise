<template>
    <div class="onboarding-step_final">
        <BaseAvatar src="/images/Boarley.svg" alt="Boarley" size="xl" />
        <h2 class="onboardoing-step_title">All Done!</h2>
        <p>Start your journey through Boardwise!</p>

        <BaseButton variant="primary" size="lg" class="onboarding-step_cta" @click="$emit('finished')">
            Let's Go
        </BaseButton>
    </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

defineEmits(['finished'])
</script>

<style scoped>
.onboarding-step_final {
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-6);
    max-width: 720px;
    width: 100%;
}
</style>
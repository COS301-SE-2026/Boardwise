<template>
    <BaseCard class="onboarding-setup onboarding-setp_final">
        <BaseAvatar src="/images/Boarley.svg" alt="Boarley" size="96" />
        <h2 class="onboardoing-step_title">All Done!</h2>
        <p>Start your journey through Boardwise!</p>

        <BaseButton variant="primary" size="lg" class="onboarding-step_cta" @click="$emit('finished')">
            Let's Go
        </BaseButton>
    </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseCard from '~/components/ui/BaseCard.vue';

defineEmits(['finished'])
</script>

<style scoped>
.onboarding-step_final {
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-4);
    max-width: 420px;
}
</style>
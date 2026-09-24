<template>
    <BaseModal
        :model-value="modelValue"
        title="Setup Complete!"
        aria-label="Setup complete"
        :closable="false"
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="setup-complete">
            <BaseImage src="/images/Boarley_cute.svg" alt="Boarley celebrating" height="96px" width="96px" fit="contain" />
            <p>
                <strong> {{ gameTitle }}</strong> is ready to play. All components are places and verified - time to roll the first player!
            </p>
        </div>

        <template #actions>
            <BaseButton @click="$emit('finish')">Back to Setup Hub</BaseButton>
        </template>
    </BaseModal>
</template>

<script setup lang="ts">
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

defineProps<{
    modelValue: boolean
    gameTitle: string
}>()

defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'finish'):void
}>()
</script>
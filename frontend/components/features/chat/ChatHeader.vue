<template>
    <BaseCard class="pa-4">
        <div class="d-flex align-center justify-space-between">
            <div class="d-flex align-center ga-3">

                <BaseAvatar
                    :src="props.conversation.avatar"
                    :name="props.conversation.name"
                    size="lg"
                />

                <div>
                    <h3  class="text-h6 mb-1">
                        {{ props.conversation.name }}
                    </h3>

                    <span class="text-body-2 text-medium-emphasis">
                        {{ props.conversation.online ? 'Online' : 'Offline' }}
                    </span>
                </div>
            </div>

            <BaseButton variant="secondary">
                About
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue';
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    }
})

</script>

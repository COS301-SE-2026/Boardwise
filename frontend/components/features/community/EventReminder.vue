<template>
    <BaseCard class="pa-4">
        <div class="  d-flex flex-column">
            <div class="  text-h6">
                <h3 class=" ">
                    {{ event.name }}
                </h3>

                <span class=" text-body-2 text-medium-emphasis">
                    {{ event.date }} | {{ event.time }}
                </span>

                 <span class=" text-body-2 text-medium-emphasis">
                    {{  evnet.locaction }}
                </span>
            </div>

            <BaseButton
                :variant="event.rsvped ? 'secondary' : 'primary'"
                @click="$emit('toggle-rsvp', event.id)"

            >
            {{ event.rsvped ? 'Going' : 'RSVP' }}
        </BaseButton>

        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

defineProps({
    event: {
        type: Object,
        required: true
    }
})

defineEmits(['toggle-rsvp'])
</script>
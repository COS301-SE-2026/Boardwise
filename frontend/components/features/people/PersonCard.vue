<template>
    <BaseCard class="cursor-pointer" @click="$emit('click', person)">
        <BaseImage 
            :src="person.avatarUrl ?? '/default-avatar.png'"
            :alt="person.username"
            height="180px"
        />

        <div class="pa-4 d-flex flex-column ga-2">
            <h3 class="card-title">@{{  person.username }}</h3>

            <p class="card-meta">
                {{  person.mutuaLabel }}
            </p>

            <BaseButton
                class="mt-auto"
                block
                :variant="person.isFriend? 'secondary' : 'primary'"
                @click.stop="$emit('friend-action', person)"
            >
                {{  person.isFriend ? 'Friends':  'Add Friend'}}
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseImage from '~/components/ui/BaseImage.vue';
import BaseCard from '~/components/ui/BaseCard.vue';

defineProps({
    person: {
        type: Object,
        required: true
    }
})

defineEmits(['click', 'friend-action'])
</script>
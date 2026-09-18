<template>
    <BaseCard clickable @click="$emit('click', person)">
        <template #media>
            <BaseImage 
                :src="person.avatarUrl ?? '/default-avatar.png'"
                :alt="person.username"
                height="180px"
            />
        </template>

        <p class="card-title">@{{ person.username }}</p>
        
        <template #actions>
            <BaseButton
                block
                :variant="person.isFriend? 'secondary' : 'primary'"
                @click.stop="$emit('friend-action', person)"
                :disabled="person.isFriend"
            >
                {{  person.isFriend ? 'Friends':  'Add Friend'}}
            </BaseButton>
        </template>
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
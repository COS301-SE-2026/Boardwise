<template>
    <div class="friend-requests" data-test="friend-requests">
        <h2 class="friend-requests__title">
            Friend Requests
            <BaseBadge variant="primary">{{ requests.length }}</BaseBadge>
        </h2>

        <div class="friends-requests__row">
            <BaseCard
                v-for="req in requests"
                :key="req.id"
                class="friend-request-card"
            >
                <div class="friend-request-card__row">
                    <BaseAvatar :src="req.sender.profilePicture" :name="req.sender.fullname" size="md" />

                    <div class="friend-request-card__info">
                        <p class="card-title">{{ req.sender.fullname }}</p>
                        <p class="card-meta">@{{ req.sender.username }}</p>
                    </div>
                </div>

                <template #actions>
                    <div class="friend-request-card__actions">
                        <BaseButton variant="primary" size="sm" @click="$emit('accept', req.id)">
                            Accept
                        </BaseButton>

                        <BaseButton variant="secondary" size="sm" @click="$emit('decline', req.id)">
                            Decline
                        </BaseButton>
                    </div>
                </template>
            </BaseCard>
        </div>
    </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({
    requests: { type: Array, default: () => [] }
})

defineEmits(['accept', 'decline'])
</script>
<template>
    <BaseCard data-test="player-card" clickable class="player-card" @click="goToProfile">
        <div class="player-card-row">
            <BaseAvatar :src="player.profilePicture" :name="displayName" size="lg" />

            <div class="player-card__info">
                <p class="card-title player-card__name">{{ displayName }}</p>
                <p class="card-meta">@{{ player.username }}</p>
            </div>
        </div>

        <template #actions>
            <div class="player-card__actions" @click.stop>
                <BaseButton 
                    v-if="computedStatus === FriendStatus.ACCEPTED"
                    variant="secondary"
                    size="sm"
                    @click="$emit('message', player.id)"
                >
                    <v-icon start size="18">mdi-message-outline</v-icon>
                    Message
                </BaseButton>

                <FriendActionButton 
                    :status="computedStatus"
                    @add="$emit('friend-request', player.id)"
                    @remove="$emit('unfriend', player.id)"
                />
            </div>
        </template>
    </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import FriendActionButton from '../people/FriendActionButton.vue'

import { FriendStatus } from '~/services/userService'

const props = defineProps({
    player: { type: Object, required: true },
    variant: {
        type: String,
        default: 'discover',
        validator: (v) => ['discover', 'friend'].includes(v)
    }
})

defineEmits(['friend-request', 'message', 'unfriend'])

const router = useRouter()

const displayName = computed(() => 
    props.player.fullName || props.player.username
)

const computedStatus = computed(() => 
    props.variant === 'friend' ? FriendStatus.ACCEPTED : props.player.status
)

const goToProfile = () => router.push(`/profile/&{props.player.id}`)
</script>
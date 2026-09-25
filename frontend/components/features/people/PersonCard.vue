<template>
    <BaseCard data-test="person-card" clickable class="person-card" @click="goToProfile">
        <div class="person-card-row">
            <BaseAvatar :src="person.profilePicture || person.avatarUrl" :name="displayName" size="lg" />

            <div class="person-card__info">
                <p class="card-title person-card__name">{{ displayName }}</p>
                <p class="card-meta">@{{ person.username }}</p>
            </div>
        </div>

        <template #actions>
            <div class="person-card__actions" @click.stop>
                <BaseButton 
                    v-if="computedStatus === FriendStatus.ACCEPTED"
                    variant="secondary"
                    size="sm"
                    @click="$emit('message', person.id)"
                >
                    <v-icon start size="18">mdi-message-outline</v-icon>
                    Message
                </BaseButton>

                <FriendActionButton 
                    :status="computedStatus"
                    @add="$emit('add-friend', person.id)"
                    @remove="$emit('unfriend', person.id)"
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
    person: { type: Object, required: true },
    variant: {
        type: String,
        default: 'discover',
        validator: (v) => ['discover', 'friend'].includes(v)
    }
})

defineEmits(['add-friend', 'message', 'unfriend'])

const router = useRouter()

const displayName = computed(() => 
    props.person.fullName || props.person.username
)

const computedStatus = computed(() => {
    if ( props.person.isFriend) {
        return FriendStatus.ACCEPTED
    } 

    if (props.variant === 'friend') {
        return FriendStatus.ACCEPTED
    }

    return props.person.status
})

const goToProfile = () => {
    router.push(`/profile/${props.person.id}`)
}
</script>
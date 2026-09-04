<template>
    <BaseButton
        :variant="variant"
        size="small"
        :disabled="status === FriendStatus.REQUESTED"
        @click="handleClick"
    >
        <v-progress-circular
            indeterminate
            color="primary"
            size="48"
            v-if="isLoading"
        />
        <p v-else>{{ label }}</p>
  </BaseButton>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { FriendStatus } from '~/services/userService'
import { useFriends } from '~/composables/useFriends';

const { isLoading } = useFriends()

const props = defineProps<{
    status: FriendStatus
}>()

const emit = defineEmits<{
    (e: 'add'): void
    (e: 'remove'): void
}>()

const label = computed(() => {
    switch(props.status) {
        case FriendStatus.ACCEPTED: return 'Unfriend'
        case FriendStatus.REQUESTED : return 'Requested'
        default: return 'Add Friend'
    }
})

const variant = computed(() => {
    switch(props.status){
        case FriendStatus.ACCEPTED: return 'secondary'
        case FriendStatus.DECLINED: return 'primary'
        case FriendStatus.REQUESTED: return 'ghost'
        default: return 'primary'
    }
})

const handleClick = () => {
    console.log("you clicked the friend action button")
    if(props.status === FriendStatus.ACCEPTED) emit('remove')
    else emit('add')
}
</script>
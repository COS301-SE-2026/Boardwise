<template>

</template>

<script setup lang="ts">
import { computed } from 'vue'
import { type FriendStatus } from '~/services/friendService'

const props = defineProps<{
    status: FriendStatus
}>()

const emit = defineEmits<{
    (e: 'add'): void
    (e: 'remove'): void
}>()

const label = computed(() => {
    switch(props.status) {
        case 'friends': return 'Friends'
        case 'pendingSent' : return 'Request Sent'
        case 'pendingReceived': return 'Respond'
        default: return 'Add Friend'
    }
})

const variant = computed(() => props.status === 'none' ? 'primary': 'secondary')

const handleClick = () => {
    if(props.status === 'friends') emit('remove')
    else if (props.status === 'none') emit('add')

    //pendingReceived 
}
</script>
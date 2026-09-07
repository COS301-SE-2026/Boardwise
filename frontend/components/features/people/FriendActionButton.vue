<template>
    <BaseButton
        :variant="variantMap[status]"
        size="small"
        :disabled="status === 'pendingSent'"
        @click="handleClick"
    >
    {{ label }}
  </BaseButton>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { type FriendStatus } from '~/services/friendService'

const props = defineProps<{
    status: FriendStatus
}>()

const emit = defineEmits<{
    (e: 'add'): void
    (e: 'remove'): void
    (e: 'respond'): void
}>()

const label = computed(() => {
    switch(props.status) {
        case 'friends': return 'Friends'
        case 'pendingSent' : return 'Request Sent'
        case 'pendingReceived': return 'Respond'
        default: return 'Add Friend'
    }
})

const variantMap: Record<FriendStatus, string> = {
    none: 'primary',
    friends: 'secondary',
    pendingSent: 'ghost',
    pendingReceived: 'accent'
} 

const handleClick = () => {
    if(props.status === 'friends') emit('remove')
    else if (props.status === 'none') emit('add')
    else if (props.status === 'pendingReceived') emit('respond')
}
</script>
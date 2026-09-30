<template>
    <div v-if="view">
        <div class="live-table-header">
            <div>
                <BaseBackButton :to="'/events'">Events</BaseBackButton>
                <h1 style="margin:8px 0 0">{{ view.name }}</h1>
            </div>

            <div class="d-flex align-center ga-3 flex-wrap">
                <div class="live-table-header__status">
                    <v-icon size="14" :color="isPaused ? 'var(--color-warning)' : 'var(--wildfire)'">mdi-circle</v-icon>
                    <span class="card-subtitle" style="margin:0">{{ isPaused ? 'PAUSED' : 'LIVE' }}</span>
                    <span class="live-table-header__timer">{{ elapsed }}</span>
                </div>

                <button
                    v-if="isHost"
                    type="button"
                    class="btn btn--primary host-controls-trigger"
                    @click="showHostControls = true"
                >
                    <v-icon size="18">mdi-tune</v-icon>
                    Host controls
                </button>
            </div>
        </div>

        <div class="live-table-layout">
            <div class="live-table-main">
                <BaseCard>
                    <p class="card-title">
                        Table Seats &amp; Roster
                        <span class="card-meta">({{ seatedCount }} of {{ view.capacity }} seated)</span>
                    </p>

                    <TableSeatGrid :seats="view.seats" @claim="handleClaim" />
                </BaseCard>

                <TableChatFeed :messages="view.messages" @send="handleSend" />
            </div>

            <div class="live-table-sidebar">
                <VenueInfoCard :venue="view.venue" :table="view.table" />
                <ShareLinkCard :url="shareUrl" />
            </div>
        </div>

        <HostControlsModal v-if="isHost" v-model="showHostControls" :event="view" @ended="handleEnded" />
    </div>

    <BaseLoadingState v-else-if="isLoading" />

    <BaseEmptyState
        v-else
        title="Table not found"
        :message="error || 'This live session may have ended.'"
    />
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

import TableSeatGrid from './TableSeatGrid.vue'
import TableChatFeed from './TableChatFeed.vue'
import VenueInfoCard from './VenueInfoCard.vue'
import ShareLinkCard from './ShareLinkCard.vue'
import HostControlsModal from './HostControlsModal.vue'

import { useLiveEvents } from '~/composables/useLiveEvents'
import { useElapsedTimer } from '~/composables/useElapsedTimer'
import { useSnackBar } from '~/composables/useSnackbar'

const props = defineProps({ eventId: { type: String, required: true } })

const router = useRouter()
const { show } = useSnackBar()

const {
    event, seats, seatedCount, messages, isJoined, isHost, isLoading, error,
    openEvent, closeEvent, claimSeat, sendMessage,
} = useLiveEvents()

// reads the user id out of the JWT payload
const userIdFromToken = (token) => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
        return payload.userId ?? payload.id ?? payload.sub ?? ''
    } catch {
        return ''
    }
}


onMounted(async () => {
    const token = localStorage.getItem('access_token') ?? ''
    try {
        await openEvent(props.eventId, { token, userId: userIdFromToken(token) })
    } catch {
        // the empty state shows the error text
    }
})

onBeforeUnmount(closeEvent)

// start time from the backend date and time, never in the future
const startedAt = computed(() => {
    const e = event.value
    if (!e?.date || !e?.time) return Date.now()
    const t = new Date(`${e.date}T${e.time}`).getTime()
    return Number.isNaN(t) ? Date.now() : Math.min(t, Date.now())
})

// old child components expect the mock shape, so adapt the backend data here
const chatMessages = computed(() =>
    messages.value.map(m => ({
        id: m.id,
        user: m.senderUsername,
        isHost: m.isHost,
        text: m.content,
        ts: Date.parse(m.createdAt),
    }))
)

const view = computed(() => {
    const e = event.value
    if (!e) return null
    return {
        id: props.eventId,
        name: e.title,
        game: '',
        venue: e.venueName ?? e.link ?? '',
        table: e.table ?? '',
        capacity: e.maxSeats,
        status: 'LIVE',
        startedAt: startedAt.value,
        seats: seats.value,
        messages: chatMessages.value,
    }
})

const elapsed = useElapsedTimer(startedAt)
const isPaused = computed(() => false) // the backend has no pause yet
const shareUrl = computed(() => `${window.location.origin}/live-events/${props.eventId}`)

const handleClaim = async () => {
    if (isJoined.value) return show('You already have a seat.', 'info')
    try {
        await claimSeat()
    } catch (e) {
        show(e?.data?.message || 'Could not claim a seat.', 'error')
    }
}

const handleSend = async (text) => {
    try {
        await sendMessage(text)
    } catch (e) {
        show(e?.data?.message || 'Could not send your message.', 'error')
    }
}

const handleEnded = () => {
    show('Session ended.', 'success')
    router.push('/events')
}
</script>
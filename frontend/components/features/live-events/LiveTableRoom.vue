<template>
    <div v-if="event">
        <div class="live-table-header">
            <div>
                <BaseBackButton :to="'/events'">Events</BaseBackButton>
                <h1 style="margin:8px 0 0">{{ event.name }}</h1>
            </div>

            <div class="d-flex align-center ga-3 flex-wrap">
                <div class="live-table-header__status">
                    <v-icon size="14" color="isPaused ? 'var(--color-warning)' : 'var(--wildfire)'">mdi-circle</v-icon>
                    <span class="card-subtitle" style="margin:0">{{ isPaused ? 'PAUSED' : 'LIVE' }}</span>
                    <span class="live-table-header__timer">{{ elapsed }}</span>
                </div>

                <BaseButton v-if="isHost" variant="secondary" size="sm" @click="showHostControls = true">
                    <v-icon start size="16">mdi-tune</v-icon> Host controls
                </BaseButton>
            </div>
        </div>

        <div class="live-table-layout">
            <div class="live-table-main">
                <BaseCard>
                    <p class="card-title">
                        Table Seats &amp; Roster
                        <span class="card-meta">({{ seatedCount }} of {{ event.capacity }} seated)</span>
                    </p>

                    <TableSeatGrid :seats="event.seats" @claim="handleClaim" />
                </BaseCard>

                <TableChatFeed :messages="event.messages" @send="handleSend" />
            </div>

            <div class="live-table-sidebar">
                <VenueInfoCard :venue="event.venue" :table="event.table" />
                <ShareLinkCard :url="shareUrl" />
            </div>
        </div>

        <HostControlsModal v-if="isHost" v-model="showHostControls" :event="event" @ended="handleEnded" />
    </div>

    <BaseEmptyState v-else title="Table not found" message="This live session may have ended." />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

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

const { getLiveEvent, claimSeat, sendMessage, currentUser } = useLiveEvents()
const event = getLiveEvent(props.eventId)
const showHostControls = ref(false)

const elapsed = useElapsedTimer(computed(() => event.value?.startedAt ?? Date.now()))
const isPaused = computed(() => event.value?.status === 'PAUSED')
const isHost = computed(() => event.value?.seats.some(s => s.isHost && s.user?.username === currentUser) ?? false)
const seatedCount = computed(() => event.value?.seats.filter(s => s.status !== 'OPEN').length ?? 0)
const shareUrl = computed(() => `boardwise.games/live-events/${props.eventId}`)

const handleClaim = () => {
  if (isPaused.value) return show('The host has paused this table.', 'warning')
  claimSeat(props.eventId)
}

const handleSend = (text) => sendMessage(props.eventId, text)

const handleEnded = () => {
  show('Session ended.', 'success')
  router.push('/events')
}
</script>
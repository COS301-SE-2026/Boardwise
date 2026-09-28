<template>
    <div v-if="event">
        <div class="live-table-header">
            <div>
                <BaseBackButton :to="'/events'">Events</BaseBackButton>
                <h1 style="margin:8px 0 0">{{ event.name }}</h1>
            </div>

            <div class="live-table-header__status">
                <v-icon size="14" color="var(--wildfire)">mdi-circle</v-icon>
                <span class="card-subtitle" style="margin:0">LIVE</span>
                <span class="live-table-header__timer">{{ elapsed }}</span>
            </div>
        </div>

        <div class="live-table-layout">
            <div class="live-table-main">
                <BaseCard>
                    <p class="card-title">
                        Table Seats &amp; Roster
                        <span class="card-meta">({{ seatedCount }} of {{ event.capacity }} seated)</span>
                    </p>

                    <TableSeatGrid :seats="event.seats" @cliam="handleClaim" />
                </BaseCard>

                <TableChatFeed :messages="event.messages" @send="handleSend" />
            </div>

            <div class="live-table-sidebar">
                <VenueInfoCard :venue="event.vanue" :table="event.table" />
                <ShareLinkCard :url="shareUrl" />
            </div>
        </div>
    </div>

    <BaseEmptyState v-else title="Table not found" message="This live session may have ended." />
</template>

<script setup>
import { computed } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import TableSeatGrid from './TableSeatGrid.vue'
import TableChatFeed from './TableChatFeed.vue'
import VenueInfoCard from './VenueInfoCard.vue'
import ShareLinkCard from './ShareLinkCard.vue'

import { useLiveEvents } from '~/composables/useLiveEvents'
import { useElapsedTimer } from '~/composables/useElapsedTimer'

const props = defineProps({ eventId: { type: String, required: true } })

const { getLiveEvent, calimSeat, sendMessage } = useLiveEvents()
const event = getLiveEvent(props.eventId)

const elapsed = useElapsedTimer(computed(() => event.value?.startedAt ?? Date.now()))
const seatedCount = computed(() => event.value?.seats.filter(s => s.status !== 'OPEN').length ?? 0)
const shareUrl = computed(() => `boardwise.games/live/${props.eventId}`)

const handleClaim = () => claimSeat(props.eventId)
const handleSend = (text) => sendMessage(props.eventId, text)
</script>
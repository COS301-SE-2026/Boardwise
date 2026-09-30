<template>
    <BaseModal v-model="open" title="Host Controls" :max-width="640" :fullscreen="mobile">
        <div class="host-controls">
            <!-- Session management -->
            <section class="host-controls__section">
                <p class="card-subtitle">Session</p>
                <div class="host-controls__row">
                    <BaseButton variant="secondary" @click="togglePause">
                        <v-icon start>{{  isPaused ? 'mdi-play' : 'mdi-pause' }}</v-icon>
                        {{  isPaused ? 'Resume table' : 'Pause table' }}
                    </BaseButton>

                    <button type="button" class="btn btn--danger-outline" @click="confirmEnd = true">
                        <v-icon start>mdi-stop-circle-outline</v-icon>
                        End session
                    </button>
                </div>
            </section>

            <!-- Capacity management -->   
            <section class="host-controls__section">
                <p class="card-subtitle">Table capacity</p>
                <p class="card-meta">Can't go below occupied seats.</p>

                <div class="capacity-stepper">
                    <BaseButton icon size="36" variant="text" :disabled="event.capacity <= minCapacity" @click="setCapacity(event.id, event.capacity - 1)">
                        <v-icon size="18">mdi-minus</v-icon>
                    </BaseButton>
                    <span class="capacity-stepper__value">{{ event.capacity }}</span>
                    <BaseButton icon size="36" variant="text" :disabled="event.capacity >= 12" @click="setCapacity(event.id, event.capacity + 1)">
                        <v-icon size="18">mdi-plus</v-icon>
                    </BaseButton>
                </div>
            </section>

            <!-- Roster -->
            <section class="host-controls__section">
                <p class="card-subtitle">Roster</p>
                <div v-for="s in seatedPlayers" :key="s.seat" class="host-controls__player">
                    <BaseAvatar :name="s.user.username" size="sm" />
                    <div class="host-controls__player-info">
                        <p class="card-subtitle" style="margin: 0">@{{  s.user.username }}</p>
                        <span class="card-meta">Seat {{ s.seat }} </span>
                    </div>
                    <BaseButton v-if="s.status === 'EN_ROUTE'" size="sm" variant="secondary" @click="checkInPlayer(event.id, s.seat)">
                        Check in
                    </BaseButton>
                    <BaseButton v-if="!s.isHost" size="sm" variant="error" @click="removePlayer(event.id, s.seat)">
                        Remove
                    </BaseButton>
                </div>
            </section>

            <!-- Announcement -->
            <section class="host-controls__section">
                <p class="card-subtitle">Announcement</p>
                <form class="table-chat-composer" @submit.prevent="announce">
                    <BaseInput v-model="announcement" placeholder="Broadcast to the table..." hide-details />
                    <BaseButton type="submit" :disabled="!announcement.trim()">Post</BaseButton>
                </form>
            </section>

            <BaseModal v-model="confirmEnd" title="End this session?" :max-width="420">
                <p class="card-meta">
                    This ends the live table for everyone. Players will be sent back to Events and this can't be undone.
                </p>

                <template #actions>
                    <BaseButton variant="secondary" @click="confirmEnd = false">Keep playing</BaseButton> 
                    <v-spacer />
                    <button type="button" class="btn btn--danger" @click="endSession">Yes, end it</button>
                </template>
            </BaseModal>
        </div>

        <template #actions>
            <v-spacer />
            <BaseButton @click="open = false">Done</BaseButton>
        </template>
    </BaseModal>
</template>

<script setup>
import { ref, computed } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

import { useLiveEvents } from '~/composables/useLiveEvents'

const open = defineModel({ type: Boolean })
const props = defineProps({ 
    event: {
        type: Object,
        required: true
    }
})
const emit = defineEmits(['ended'])

const { setStatus, setCapacity, checkInPlayer, removePlayer, postAnnouncement } = useLiveEvents()

const confirmEnd = ref(false)
const announcement = ref('')

const isPaused = computed(() => props.event.status === 'PAUSED')
const seatedPlayers = computed(() => props.event.seats.filter(s => s.user))
const minCapacity = computed(() => Math.max(3, seatedPlayers.value.length))

const togglePause = () => setStatus(props.event.id, isPaused.value ? 'LIVE' : 'PAUSED')

const endSession = () => {
    setStatus(props.event.id, 'ENDED')
    confirmEnd.value = false
    open.value = false
    emit('ended')
}

const announce = () => {
    postAnnouncement(props.event.id, announcement.value)
    announcement.value = ''
}

</script>
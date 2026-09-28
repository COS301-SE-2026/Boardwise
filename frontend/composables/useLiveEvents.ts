import { ref, computed } from 'vue'
import { createSharedComposable } from '@vueuse/core'
import { mockLiveEvents, type LiveEvent } from '~/services/liveEvents.mock'

interface LiveEventPayload {
    name: string
    game: string
    venue: string
    table: string
    capacity: number
}

const _useLiveEvents = () => {
    const liveEvents = ref(structuredClone(mockLiveEvents))
    const isLoading = ref(false)
    const error = ref('')

    const fetchLiveEvents = async () => {
        isLoading.value = true
        await new Promise(r => setTimeout(r, 400))
        isLoading.value = false
    }

    const getLiveEvent = (id: string) => 
        computed(() => liveEvents.value.find(e => e.id === id) ?? null)

    const claimSeat = (eventId: string, username ='You') => {
        const event = liveEvents.value.find(e => e.id === eventId)
        if(!event) return

        const seat = event.seats.find(s => s.status === 'OPEN')
        if(seat) {
            seat.user = { username }
            seat.status = 'SEATED'
        }
    }

    const sendMessage = (eventId: string, text: string ) => {
        const event = liveEvents.value.find(e => e.id === eventId)
        if (!event || !text.trim()) return

        event.messages.push({
            id: crypto.randomUUID(),
            user: 'You',
            isHost: false,
            text: text.trim(),
            ts: Date.now()
        })
    }

    const createLiveEvent = async (payload: LiveEventPayload) => {
        isLoading.value = true
        await new Promise(r => setTimeout(r, 500))

        const newEvent: LiveEvent = {
            id: crypto.randomUUID(),
            name: payload.name,
            game: payload.game,
            venue: payload.venue,
            table: payload.table,
            status: 'LIVE',
            capacity: payload.capacity,
            seats: Array.from({ length: payload.capacity ?? 4 }, (_, i) => ({
                seat: i + 1,
                user: i === 0 ? { username: 'You'} : null,
                isHost: i === 0,
                status: i === 0 ? 'SEATED' : 'OPEN'
            })),
            messages: [],
            startedAt: Date.now()
        }
        liveEvents.value.unshift(newEvent)
        isLoading.value = false
        return newEvent
    }

    return {
        liveEvents, 
        isLoading, 
        error, 
        fetchLiveEvents, 
        getLiveEvent, 
        claimSeat, 
        sendMessage, 
        createLiveEvent
    }
}

export const useLiveEvents = createSharedComposable(_useLiveEvents)
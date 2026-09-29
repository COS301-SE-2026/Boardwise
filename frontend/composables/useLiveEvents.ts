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

    const currentUser = 'You'
    const findEvent = (id: string) => liveEvents.value.find(e => e.id === id)

    const activeLiveEvents = computed(() =>  liveEvents.value.filter(e => e.status !== 'ENDED'))

    const setStatus = (eventId: string, status: 'LIVE' | 'PAUSED' | 'ENDED') => {
        const event = findEvent(eventId)
        if (event) event.status = status
    }

    const checkInPlayer = (eventId: string, seatNo: number) => {
        const seat = findEvent(eventId)?.seats.find(s => s.seat === seatNo)
        if (seat?.status === 'EN_ROUTE') seat.status = 'SEATED'
    }

    const removePlayer = (eventId: string, seatNo: number) => {
        const seat = findEvent(eventId)?.seats.find(s => s.seat === seatNo)
        if (seat && !seat.isHost) {
            seat.user = null
            seat.status = 'OPEN'
        }
    }

    const setCapacity = (eventId: string, capacity: number) => {
        const event = findEvent(eventId)
        if (!event) return
        while (event.seats.length < capacity) {
            event.seats.push({ seat: event.seats.length + 1, user: null, status: 'OPEN' })
        }

        while (event.seats.length > capacity && event.seats.at(-1)?.status === 'OPEN') {
            event.seats.pop()
        }
        event.capacity = event.seats.length
    }

    const postAnnouncement = (eventId: string, text: string) => {
        const event = findEvent(eventId)
        if (!event || !text.trim()) return
        event.messages.push({
            id: crypto.randomUUID(),
            user: event.seats.find(s => s.isHost)?.user?.username ?? currentUser,
            isHost: true,
            text: text.trim(),
            ts: Date.now()
        })
    }

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
        currentUser,
        activeLiveEvents,
        fetchLiveEvents, 
        getLiveEvent, 
        claimSeat, 
        sendMessage, 
        createLiveEvent,
        setStatus,
        checkInPlayer,
        removePlayer,
        setCapacity,
        postAnnouncement,
    }
}

export const useLiveEvents = createSharedComposable(_useLiveEvents)
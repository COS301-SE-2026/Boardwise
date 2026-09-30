import { ref, computed } from 'vue'
import { createSharedComposable } from '@vueuse/core'
import {
    LiveEventsService,
    type LiveEventAttendee,
    type LiveEventMessage,
    type LiveEventSocketHandlers,
} from '~/services/liveEventsService'

const DURATION: Record<string, string> = { short: 'SHORT', standard: 'STANDARD', marathon: 'MARATHON' }
const TONE: Record<string, string> = { casual: 'CASUAL', learn: 'LEARN', competitive: 'COMPETITIVE' }
const PRIVACY: Record<string, string> = { public: 'PUBLIC_EVENT', private: 'PRIVATE_EVENT' }
const STATUS_TO_UI: Record<string, string> = { ARRIVED: 'SEATED', EN_ROUTE: 'EN_ROUTE', NOT_ARRIVED: 'EN_ROUTE', JOINED: 'EN_ROUTE' }
const UI_TO_STATUS = { SEATED: 'ARRIVED', EN_ROUTE: 'EN_ROUTE' } as const

const _useLiveEvents = () => {
    const event = ref<any>(null)
    const roster = ref<LiveEventAttendee[]>([])
    const messages = ref<LiveEventMessage[]>([])
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
        try {
            const list = await LiveEventsService.listLiveEvents()
            liveEventsList.value = list.map((e: any) => {
                const people = e.liveAttendees?.attendees ?? []
                return {
                    id: e.id,
                    name: e.title,
                    game: e.gameTitle ?? '',
                    venue: e.venueName,
                    table: e.table,
                    capacity: e.maxSeats,
                    seats: Array.from({ length: e.maxSeats }, (_, i) => ({
                        seat: i + 1,
                        user: people[i] ? { username: people[i].username ?? people[i].userId } : null,
                        status: people[i] ? (STATUS_TO_UI[people[i].status] ?? 'SEATED') : 'OPEN',
                    })),
                }
            })
        } finally {
            isLoading.value = false
        }
    }

    const activeLiveEvents = computed(() => liveEventsList.value)

    const seats = computed(() =>
        Array.from({ length: event.value?.maxSeats ?? 0 }, (_, i) => {
            const a: any = roster.value[i]
            return a
                ? {
                    seat: i + 1,
                    user: { id: a.userId, username: a.username ?? a.userId },
                    isHost: a.isHost,
                    status: STATUS_TO_UI[a.status] ?? 'SEATED',
                }
                : { seat: i + 1, user: null, isHost: false, status: 'OPEN' }
        })
    )

    const seatedCount = computed(() => roster.value.length)

    const stopSocket = () => {
        disconnect?.()
        disconnect = undefined
    }

    const startSocket = () => {
        stopSocket()

        const handlers: LiveEventSocketHandlers = {
            onRoster: (a) => { roster.value = a },
            onError: (e) => { error.value = e },
        }

        // only seated players can read the chat
        if (isJoined.value) {
            handlers.onHistory = (list) => {
                const ids = new Set(list.map(m => m.id))
                messages.value = [...list, ...messages.value.filter(m => !ids.has(m.id))]
            }
            handlers.onMessage = (m) => {
                if (!messages.value.some(x => x.id === m.id)) messages.value.push(m)
            }
        }

        disconnect = LiveEventsService.connect(eventId, token, handlers)
    }

    const openEvent = async (id: string, auth: { token: string; userId: string }) => {
        eventId = id
        token = auth.token
        myUserId.value = auth.userId
        isLoading.value = true
        error.value = ''
        messages.value = []
        try {
            const res = await LiveEventsService.getLiveEvent(id)
            event.value = res.details
            roster.value = res.details.liveAttendees?.attendees ?? []

            if (isJoined.value) {
                try {
                    const history = await LiveEventsService.getMessages(id)
                    messages.value = history
                } catch (e) {
                    console.warn('Could not preload chat history', e)
                }
            }

            startSocket()
        } catch (e: any) {
            error.value = e?.data?.message ?? 'Could not load this event'
            throw e
        } finally {
            isLoading.value = false
        }
    }

    const closeEvent = () => {
        stopSocket()
        event.value = null
        roster.value = []
        messages.value = []
    }

    const claimSeat = async () => {
        await LiveEventsService.joinLiveEvent(eventId)
        const res = await LiveEventsService.getLiveEvent(eventId)
        roster.value = res.details.liveAttendees?.attendees ?? []
        startSocket() // reconnect so the chat subscription is included
    }

    const sendMessage = async (text: string) => {
        if (!text.trim()) return
        // no local push: the sender gets it back over the socket
        await LiveEventsService.postMessage(eventId, text.trim())
    }

    const postAnnouncement = sendMessage

    const setMyStatus = (uiStatus: 'SEATED' | 'EN_ROUTE') =>
        LiveEventsService.updateStatus(eventId, UI_TO_STATUS[uiStatus])

    const deleteEvent = async () => {
        await LiveEventsService.deleteLiveEvent(eventId)
        closeEvent()
    }

    const createLiveEvent = async (f: any) => {
        isLoading.value = true
        try {
            const online = f.format === 'virtual'
            const payload = {
                boardgameId: f.gameId,
                title: f.name,
                type: online ? 'ONLINE' : 'IN_PERSON',
                venueName: online ? null : f.venue,
                table: online ? null : f.table,
                link: online ? f.venue : null,
                duration: DURATION[f.duration] ?? 'STANDARD',
                maxSeats: f.capacity,
                tone: TONE[f.style] ?? 'CASUAL',
                privacy: PRIVACY[f.privacy] ?? 'PUBLIC_EVENT',
                automaticApproval: f.autoApprove,
            }
            const res = await LiveEventsService.createLiveEvent(payload)
            return { id: res.id as string }
        } finally {
            isLoading.value = false
        }
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
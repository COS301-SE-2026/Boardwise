import { ref } from 'vue'
import {
    EventService, 
    type EventResponse,
    type InviteItem
} from '~/services/eventService'

export const useEvents = () => {
    const events = ref<EventResponse[]>([])
    const invites = ref<InviteItem[]>([])
    const inviteCount = ref<number>(0)
    const isLoading = ref<boolean>(false)
    const error = ref<string>('')

    //AC-EVT-01
    const fetchEvents = async (name?: string) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.getAllEvents(name)
            events.value = data.result
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to load events'
            events.value = []
        } finally {
            isLoading.value = false
        }
    }

    //AC-EVT-02
    const createEvent = async (eventInfo: object, image?: File) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.createEvent(eventInfo, image)
            events.value.unshift(data.data)
            return data.data
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to create event'
            throw err
        } finally {
            isLoading.value = false
        }
    }

    //AC-EVT-03
    const updateEvent = async (eventId: string, eventInfo: object, image?: File) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.updateEvent(eventId, eventInfo)
            const index = events.value.findIndex(e => e.id === eventId)
            if (index !== -1)
            {
                events.value[index] = data.data
            }
            return data.data
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to update event'
            throw err
        } finally {
            isLoading.value = false
        }
    }

    //AC-EVT-04
    const cancelEvent = async (eventId: string) => {
        isLoading.value = true
        error.value = ''

        try {
            await EventService.cancelEvent(eventId)
            const index = events.value.findIndex(e => e.id === eventId)
            if (index !== -1)
            {
                const event = events.value[index]
                if (event) {
                    event.eventStatus = 'CANCELLED'
                }
            }
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to cancel event'
            throw err
        } finally {
            isLoading.value = false
        }
    }

    //AC-EVT-05
    const rsvpToEvent = async (eventId: string) => {

    }

    //AC-EVT-06
    const deRsvpToEvent = async (eventId: string) => {

    }

    //AC-EVT-07
    const fetchInvites = async () => {

    }

    //AC-EVT-08
    const inviteUser = async (invitee: string, eventId: string) => {

    }

    //AC-EVT-09
    const respondToInvite = async (eventId: string, status: 'accept' | 'decline') => {

    }
}
import { ref } from 'vue'
import { createSharedComposable } from '@vueuse/core'
import { useSnackBar } from './useSnackbar'
import {
    EventService, 
    type EventResponse,
    type InviteItem
} from '~/services/eventService'

const { show } = useSnackBar()

const _useEvents = () => {
    const events = ref<EventResponse[]>([])
    const invites = ref<InviteItem[]>([])
    const inviteCount = ref<number>(0)
    const isLoading = ref<boolean>(false)
    const error = ref<string>('')
    const page = ref<number>(1)

    //AC-EVT-01
    const fetchEvents = async (name?: string) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.getAllEvents(name, page.value)
            events.value = data.result
            return events.value;
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to load events'
            events.value = []
        } finally {
            isLoading.value = false
        }
    }

    const fetchEventbyId = async (id: string) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.getEvent(id);
            return data.data;
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to load events'
            throw err
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
            const data = await EventService.updateEvent(eventId, eventInfo,image)
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
        error.value = ''

        try {
            const data = await EventService.rsvpToEvent(eventId)
            const index = events.value.findIndex(e => e.id === eventId)
            if(index !== -1)
            {
                events.value[index] = data.data
            }
            return data.data
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to RSVP'
            throw err
        } 
    }

    //AC-EVT-06
    const deRsvpToEvent = async (eventId: string) => {
        error.value = ''

        try {
            const data = await EventService.deRsvpToEvent(eventId)
            const index = events.value.findIndex(e => e.id === eventId)
            if(index !== -1)
            {
                events.value[index] = data.data
            }
            return data.data
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to de-RSVP'
            throw err
        } 
    }

    //AC-EVT-07
    const fetchInvites = async () => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await EventService.getUserInvites()
            console.log('Raw invites response:', data) 
            invites.value = data.invites;
            inviteCount.value = data.inviteCount
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to load invites'
            invites.value = []
        } finally {
            isLoading.value = false
        }
    }

    //AC-EVT-08
    const inviteUser = async (invitee: string, eventId: string) => {
        error.value = ''

        try {
            await EventService.inviteUser(invitee, eventId)
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to send to invite'
            throw err
        }
    }

    //AC-EVT-09
    const respondToInvite = async (eventId: string, status: 'accept' | 'decline') => {
        error.value = ''

        try {
            await EventService.respondToInvite(eventId, status)
            invites.value = invites.value.filter(i => i.event.id !== eventId)
            inviteCount.value = invites.value.length
        }catch (err: any) {
            error.value = err.data?.message || 'Failed to respond to invite'
            throw err
        }

        error.value = ''
    }

    return {
        events, 
        invites, 
        inviteCount, 
        isLoading, 
        page,
        error, 
        fetchEvents,
        fetchEventbyId,
        createEvent,
        updateEvent,
        cancelEvent,
        rsvpToEvent,
        deRsvpToEvent,
        fetchInvites,
        inviteUser,
        respondToInvite
    }
}
export const useEvents = createSharedComposable(_useEvents)
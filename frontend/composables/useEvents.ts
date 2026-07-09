import { ref } from 'vue'
import EventGrid from '~/components/features/events/EventGrid.vue'
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

    }

    //AC-EVT-02
    const createEvent = async (eventInfo: object, image?: File) => {

    }

    //AC-EVT-03
    const updateEvent = async (eventId: string, eventInfo: object, image?: File) => {

    }

    //AC-EVT-04
    const cancelEvent = async (eventId: string) => {

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
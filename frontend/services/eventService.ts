export interface EventResponse {
    id: string;
    name: string;
    description: string;
    imageUrl: string | null;
    startTime: string;
    endTime: string;
    attendeeCount: string;
    location: string;
    visibility: 'PUBLIC' | 'PRIVATE';
    eventStatus: 'OPEN' | 'CLOSED' | 'CANCELLED' | 'FULLY_BOOKED';
    rsvpStatus: 'ATTENDING' | 'NOT_ATTENDING' | 'INVITED' | 'REQUESTED';
    host: {
        username: string;
        imageUrl: string | null;
    }
    games: {
        id: string;
        title: string;
        description: string;
        imageUrl: string;
        genres: string[]
    }[];
}

export interface GetEventsResponse {
    message: string;
    result: EventResponse[];
}

export interface SingleEventResponse {
    message: string;
    data: EventResponse;
}

export interface InviteItem {
    status: string;
    host: {
        username: string;
        imageUrl: string | null;
    };
    event: {
        eventId: string;
        eventName: string;
        eventImg: string | null;
        eventDate: string;
    };
}

export interface GetInvitesResponse {
    message: string;
    inviteCount: number;
    data: InviteItem[];
}

export interface MessageResponse {
    message: string;
}

export const EventService = {

    // Note:  All functions below is based on events service contract
    // AC-EVT-01: Get all events
    getAllEvents(name?: string) {
        const { $api } = useNuxtApp()
        return $api<GetEventsResponse>('community/', {
            method: 'GET',
            query: name? { name } : {}
        })
    },

    // AC-EVT-02: Create event
    createEvent(eventInfo: object,  image?: File) {
        const { $api } = useNuxtApp()
        const formData = new FormData()
        formData.append('Eventnfo', new Blob([JSON.stringify(eventInfo)],{
            type: 'application/json'
        }))
        if (image) formData.append('EventImage', image)
        return $api<SingleEventResponse>('community/', {
            method: 'POST',
            body: formData
        })
    },

    // AC-EVT-03: Update event 
    updateEvent(eventId: string, eventInfo?: object, image?: File) {
        const { $api } = useNuxtApp()
        const formData = new FormData()
        if (eventInfo) {
            formData.append('Eventnfo', new Blob([JSON.stringify(eventInfo)],{
                type: 'application/json'
            }))
        }
        if (image) formData.append('EventImage', image)
        return $api<SingleEventResponse>(`community/${eventId}`, {
            method: 'PATCH',
            body: formData
        })
    },

    // AC-EVT-04: Cancel event
    cancelEvent(eventId: string) {
        const { $api } = useNuxtApp()
        return $api<MessageResponse>(`community/${eventId}`, {
            method: 'DELETE'
        })
    },

    // AC-EVT-05: RSVP to event
    rsvpToEvent(eventId: string) {
        const { $api } = useNuxtApp()
        return $api<SingleEventResponse>(`community/${eventId}`, {
            method: 'POST'
        })
    },

    // AC-EVT-06: De-RSVP from event
    deRsvpToEvent(eventId: string) {
        const { $api } = useNuxtApp()
        return $api<SingleEventResponse>('community/', {
            method: 'PATCH',
            body: { eventId }
        })
    },

    // AC-EVT-07: Get user event invites
    getUserInvites(){
        const { $api } = useNuxtApp()
        return $api<GetInvitesResponse>(`community/invite`, {
            method: 'GET'
        })
    },

    // AC-EVT-08: Invite user to event
    inviteUser(invitee: string, eventId: string) {
        const { $api } = useNuxtApp()
        return $api<MessageResponse>('community/invite', {
            method: 'POST',
            body: { invitee, eventId }
        })
    },

    // AC-EVT-09: Respond to invite (status: "accept" | "decline")
    respondToInvite(eventId: string, status: 'accept' | 'decline') {
        const { $api } = useNuxtApp()
        return $api<MessageResponse>(`community/invite/${eventId}`, {
            method: 'PATCH',
            query: { status }
        })
    }
}
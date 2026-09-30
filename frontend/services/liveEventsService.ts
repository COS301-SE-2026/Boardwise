import { Client, type StompSubscription} from '@stomp/stompjs'

export interface LiveEventAttendee{
    userId: string;
    status: string;
    isHost: boolean;
}

export interface LiveEventMessage{
    id: string; 
    eventId: string;
    senderId: string;
    senderUsername: string;
    content: string;
    isHost: boolean;
    createdAt: string;
}

export interface CreateLiveEventPayload{
    boardgameId: string;
    hostId: string;
    title: string;
    type: string;
    venueName: string | null;
    table: string | null;
    link: string | null;
    date: string | null;
    time: string | null;
    duration: string; 
    privacy: string;
    automaticApproval:boolean;
    liveAttendees: LiveEventAttendee[];
}

export interface LiveEventSocketHandlers{
    onHistory?: (messages: LiveEventMessage[]) => void;
    onMessage: (msg: LiveEventMessage) =>void;
    onRoster: (attendees: LiveEventAttendee[]) =>void;
    onError?: (reason: string) =>void;
}

export const LiveEventService ={
    // CREATE LIVE EVENT
    createLiveEvent(data: CreateLiveEventPayload){
        const { $api } = useNuxtApp()
        return $api<any>('community/live-event',{ method: 'POST', body:data});
    },

    //GET LIVE EVENT BY ID
    getLiveEvent(id:string){
        const {$api} = useNuxtApp();
        return $api<{message: string; details: any}>(`community/live-event/${id}`);
    },
    
    //DELETE LIVE EVENT
    deleteLiveEvent(id: string){
        const { $api } = useNuxtApp();
        return $api<any>(`community/live-event/${id}`, {method: 'DELETE'});
    },

    joinLiveEvent(id: string){
        const { $api } = useNuxtApp();
        return $api<any>(`community/live-event/${id}/join`, {method: 'POST'})
    },

    updateStatus(id:string, status: string){
        const { $api } = useNuxtApp();
        return $api<any>(`community/live-event/${id}/status`, {
            method: 'PUT',
            body: { status }
        })
    },

    async getMessages(id:string, after?: string){
        const { $api} = useNuxtApp();
        const res = await $api<{messages: LiveEventMessage[]}>(`community/live-event/${id}/messages`, {
            query: after? { after}: undefined
        });
        return res.messages;
    },

    postMessage(id:string, content: string){
        const { $api} = useNuxtApp();
        return $api<any>(`community/live-event/${id}/message`, {
            method: 'POST',
            body: { content }
        });
    },

    connect(id: string, token: string, h: LiveEventSocketHandlers){
        const config = useRuntimeConfig();
        const brokerURL = config.public.wsBaseUrl as string;

        let subs: StompSubscription[] = []

        const client  = new Client({
            brokerURL,
            connectHeaders:{
                Authorization: `Bearer ${token}`
            },
            reconnectDelay: 3000,

            onConnect: async () =>{
                //subscribe then load history 
                subs = [
                    client.subscribe(`/topic/live-event/${id}/messages`, f=> h.onMessage(JSON.parse(f.body))),
                    client.subscribe(`/topic/live-event/${id}/roster`, f=> h.onRoster(JSON.parse(f.body).atendees))
                ];

                try{
                    const history = await LiveEventService.getMessages(id);
                    h.onHistory?.(history);
                }catch( e: any){
                    h.onError?.(e.ata?.message?? 'Could not load messages')
                }
            },

            onStompError: f => h.onError?. (f.headers['message']?? 'Socket error')
        });

        client.activate();

        return () =>{
            subs.forEach(s => s.unsubscribe());
            client.deactivate();
        }
    }

}
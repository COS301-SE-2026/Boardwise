import { Client, type IMessage } from "@stomp/stompjs";
import { type DirectMessage } from "~/composables/usePrivateChat";

// how to deal with received messages
type MessageHandler = (payload: any) => void;

interface Subscription{
    callback: MessageHandler;
    referenceCount: number;
    stompSub: ReturnType<Client['subscribe']> | null;
}

let client: Client | null = null;
const subscriptions = new Map<string, Subscription>(); // track all subscriptions to an endpoint
const isConnected = useState('socket-connected', () => false);
const connectedBefore = useState('socket-connected-before', () => false);
const reconnectHooks: Array<() => void> = [];

function reSubToAll(){
    if(!client?.connected) return;

    for(const [dest, sub] of subscriptions){
        sub.stompSub = client.subscribe(dest, (msg: IMessage) => {
            sub.callback(JSON.parse(msg.body));
        })
    }
}

export function useStomp(){
    function connect(){
        if(client) return;

        const token = import.meta.client ? localStorage.getItem('access_token') : null;
        const brokerURL = useRuntimeConfig().public.wsBaseUrl;

        client = new Client({
            brokerURL,
            connectHeaders:{
                Authorization: `Bearer ${token}`
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            isConnected.value = true;
            if(connectedBefore.value){
                reSubToAll();
                reconnectHooks.forEach(hook => hook())
            }
                

            connectedBefore.value = true;
        };

        client.onDisconnect = () => isConnected.value = false;
        client.onStompError = (frame) => console.error("Stomp reported error: ", frame.headers['message'], frame.body);
        client.activate();
    };

    function disconnect(){
        client?.deactivate();
        client = null;
        subscriptions.clear();
        isConnected.value = false;
        connectedBefore.value = false;
    };

    function subscribe(dest: string, callback: MessageHandler){
        const exists = subscriptions.get(dest);
        if(exists){ // already subscribed
            exists.referenceCount++;
            return;
        }

        const sub: Subscription = {
            callback,
            referenceCount: 1,
            stompSub: null
        };
        subscriptions.set(dest, sub);

        if(client?.connected){
            sub.stompSub = client.subscribe(dest, (msg: IMessage) => {
                callback(JSON.parse(msg.body));
            });
        }
    };

    function unsubscribe(dest: string){
        const sub = subscriptions.get(dest);
        if(!sub) return;

        sub.referenceCount--;
        if(sub.referenceCount < 1){
            sub.stompSub?.unsubscribe();
            subscriptions.delete(dest);
        }
    };

    function onReconnectHook(func: () => void){
        reconnectHooks.push(func);
        onUnmounted(() => {
            const index = reconnectHooks.indexOf(func);
            if(index !== -1)
                reconnectHooks.splice(index, 1);
        })
    }

    function sendPrivateMessage(message: DirectMessage){
        if(isConnected.value && client?.connected){
            client.publish({
                destination: '/app/chat/direct',
                body: JSON.stringify(message)
            })
        }
    }

    return {
        isConnected,
        connect,
        disconnect,
        subscribe,
        unsubscribe,
        sendPrivateMessage,
        onReconnectHook
    };
}
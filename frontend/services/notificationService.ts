const USE_MOCK = true

export interface NotificationActor {
    id: string
    username: string
    profilePicture?: string
}

export interface NotificationTarget {
    id: string
    name: string
}

export interface Notification {
    id: string
    type: string
    message: string
    read: boolean
    createdAt: string
    actor: NotificationActor | null
    target: NotificationTarget | null
    link: string | null
}

interface RawNotification {
    id: string
    type?: string
    message?: string
    read?: boolean 
    isRead?: boolean 
    createdAt?: string 
    timestamp?: string 
    actor?: NotificationActor | null 
    sender?: NotificationActor | null 
    target?: NotificationTarget | null 
    link?: string | null
}

interface NotificationResponse { 
    notifications?: RawNotification[] 
    content?: RawNotification[] 
}

const normalize = (raw: RawNotification): Notification => ({
    id: raw.id, 
    type: String(raw.type ?? '').toUpperCase(), 
    message: raw.message ?? '', 
    read: Boolean(raw.read ?? raw.isRead ?? false), 
    createdAt: 
        raw.createdAt ?? 
        raw.timestamp ?? 
        new Date().toISOString(), 
    actor: raw.actor ?? raw.sender ?? null, 
    target: raw.target ?? null, 
    link: raw.link ?? null
})

const ago = (minutes: number): string => new Date(Date.now() - minutes * 60_000).toISOString()

let mockStore: RawNotification[] = [ 
    { id: 'n1', type: 'FRIEND_REQUEST', read: false, createdAt: ago(4), actor: { id: 'u2', username: 'meeplemaster' } }, 
    { id: 'n2', type: 'EVENT_RSVP', read: false, createdAt: ago(45), actor: { id: 'u3', username: 'alex_games' }, target: { id: '12', name: 'Catan Night' } }, 
    { id: 'n3', type: 'FRIEND_CONFIRMATION', read: false, createdAt: ago(60 * 5), actor: { id: 'u4', username: 'bob' } }
]

const wait = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms))

// TODO: Connect API to this userservice

async function request<T>( 
    path: string, 
    options: Parameters<typeof $fetch<T>>[1] = {}
): Promise<T> { 
    const config = useRuntimeConfig() 
    return $fetch<T>(path, {
        baseURL: config.public.apiBase, 
        headers: { 
            Authorization: `Bearer ${localStorage.getItem('access_token')}`, 
            ...options.headers 
        }, 
            ...options 
        }) 
    }

export const notificationService = {
    async list(): Promise<Notification[]> {
        if(USE_MOCK) {
            await wait(400)

            return mockStore
                .map(normalize)
                .sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        }

        const res = await request<RawNotification[] | NotificationResponse >('/notifications')
        const items = Array.isArray(res) ? res : res.notifications ?? res.content ?? []
        
        return items 
            .map(normalize)
            .sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    },

    async markRead(id: string): Promise<void> {
        if (USE_MOCK) {
            await wait(400)

            mockStore = mockStore.map((notification) => notification.id === id 
            ? { ...notification, read: true } : notification
            )

            return
        }

        await request<void>(`/notifications/${id}/read`,
            {
                method: 'PATCH'
            }
        )
    },

    async markAllRead(): Promise<void> {
        if (USE_MOCK) {
            await wait(150)

            mockStore = mockStore.map((notification) => ({
                ...notification,
                read: true
            }))
                return
        }

        await request<void>('/notifications/read-all',
            {
                method: 'PATCH'
            }
        )
    }
}
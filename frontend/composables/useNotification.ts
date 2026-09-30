import { computed } from 'vue'

import { notificationService } from '~/services/notificationService'
import type { Notification } from '~/services/notificationService'

type NotificationMeta = {
    icon: string
    color: string
    text?: (notification: Notification) => string
    link?: (notification: Notification) => string
}

const actorName = (notification: Notification): string => notification.actor?.username
    ? `@${notification.actor.username}` : 'Someone'

const targetName = (notification: Notification, fallback: string ): string => notification.target?.name || fallback

export const NOTIFICATION_TYPES: Record<string, NotificationMeta> = {
    FRIEND_REQUEST: {
        icon: 'mdi-account-plus-outline',
        color: 'var(--color-primary)',
        text: (notification) => `${actorName(notification)} sent you a friend request`,
        link: () => '/profile'
    },

    FRIEND_CONFIRMATION: {
        icon: 'mdi-account-check-outline',
        color: 'var(--color-success)',
        text: (notification) => `${actorName(notification)} accepted your friend request`,
        link: (notification) => notification.actor?.id ? '/profile/${notification.actor.id}' : '/profile'
    },

    EVENT_RSVP: {
        icon: 'mdi-calendar-check-outline',
        color: 'var(--color-secondary)',
        text: (notification) => `${actorName(notification)} is going to ${targetName(notification, 'your event')}`,
        link: (notification) => notification.target?.id ? '/events/detail/${notification.target.id}' : '/events'
    },

    EVENT_INVITE: {
        icon: 'mdi-calendar-star',
        color: 'var(--color-secondary)',
        text: (notification) => `${actorName(notification)} invited you to ${targetName(notification, 'an event')}`,
        link: (notification) => notification.target?.id ? '/events/detail/${notification.target.id}' : '/events'
    },

    COMMUNITY_INVITE: {
        icon: 'mdi-account-group-outline',
        color: 'var(--color-primary)',
        text: (notification) => `${actorName(notification)} invited you to join ${targetName(notification, 'a community')}`,
        link: () => '/social'
    }
}

const FALLBACK: NotificationMeta = {
    icon: 'mdi-bell-outline',
    color: 'var(--color-text-muted)'
}

export const getNotificationMeta = (notification: Notification): NotificationMeta => NOTIFICATION_TYPES[notification.type] ?? FALLBACK
export const describeNotification = (notification: Notification): string => notification.message || NOTIFICATION_TYPES[notification.type]?.text?.(notification) || 'You have a new notification'
export const resolveNotificationLink = (notification: Notification): string | null => notification.link || NOTIFICATION_TYPES[notification.type]?.link?.(notification) || null

export function useNotifications() {
    const notifications = useState<Notification[]>('notifications:list', () => [])
    const isLoading = useState<boolean>('notifications:loading', () => false)
    const error = useState<string>('notifications:error', () => '')
    const loaded = useState<boolean>('notifications:loaded', () => false)
    const unreadCount = computed<number>(() => notifications.value.filter((notification) => !notification.read).length)

    async function fetchNotifications({ silent = false}: { silent?: boolean } = {}): Promise<void> {
        if(!silent) {
            isLoading.value = true
        }

        error.value = ''

        try {
            notifications.value = await notificationService.list()
            loaded.value = true
        } catch (err) {
            console.error('Failed to load notifications:', err)

            if (!loaded.value) {
                error.value = 'Something went wrong loading your notifications.'
            }
        } finally {
            isLoading.value = false
        }
    }

    async function markAsRead(id:string): Promise<void> {
        const item = notifications.value.find((notification: Notification) => notification.id === id)

        if(!item || item.read) {
            return
        }

        item.read = true

        try {
            await notificationService.markRead(id)
        } catch (err) {
            console.error('Failed to mark notification as read:', err)
            item.read = false
        }
    }

    async function markAllAsRead(): Promise<void> {
        const unreadIds: string[] = notifications.value 
            .filter((notification: Notification) => !notification.read)
            .map((notification: Notification) => notification.id)

        if(!unreadIds.length) {
            return
        }

        notifications.value.forEach((notification: Notification) => {notification.read = true})

        try {
            await notificationService.markAllRead()
        } catch (err) {
            console.error('Failed to mark all as read:', err)

            notifications.value.forEach((notification: Notification) => {
                if (unreadIds.includes(notification.id)) {
                    notification.read = false
                }
            }
        )
        }
    }

    function addNotification(raw: Notification): void {
        if(notifications.value.some((notification: Notification) => notification.id === raw.id)
        ) {
            return
        }

        notifications.value = [
            {
                ...raw,
                read: false
            },
            ...notifications.value
        ]
    }   

    return {
        notifications,
        isLoading,
        error,
        loaded,
        unreadCount,
        fetchNotifications,
        markAsRead,
        markAllAsRead,
        addNotification
    }
}

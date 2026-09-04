// Friends

import type { FriendStatus } from "./userService"

export interface FriendDTO {
    id: string
    username: string
    fullname: string
    profilePicture: string
}

export interface FriendListDTO{
    message:string 
    friends: FriendDTO[]
    mutuals: FriendDTO[] | null
}

export interface FriendRequestResponse {
    message: string
}

export interface FriendRequestItem {
    id: string // reqested id for db 
    sender: FriendDTO
}

export interface FriendRequestsDTO {
    message: string
    requests: FriendRequestItem[]
}

// Notifications

export enum NotificationType {
    EVENT_INVITE = 'EVENT_INVITE',
    FRIEND_REQUEST = 'FRIEND_REQUEST',
    FRIEND_CONFIRMATION = 'FRIEND_CONFIRMATION',
}

export interface EventHostInfo {
    username: string
    imageUrl: string | null
}

export interface EventInviteInfo {
    id: string
    name: string
    image: string | null
    date: string
}

export interface InviteNotification {
    type: NotificationType.EVENT_INVITE
    host: EventHostInfo
    event: EventInviteInfo
}

export interface FriendRequestNotification {
    type: NotificationType.FRIEND_REQUEST
    request: FriendRequestItem
}

export interface FriendConfirmationNotification {
    type: NotificationType.FRIEND_CONFIRMATION
    friend: FriendDTO
}

export type NotificationItem =
    | InviteNotification
    | FriendRequestNotification
    | FriendConfirmationNotification

export interface NotificationsDTO {
    message: string
    notifications: NotificationItem[]
}

export interface GameInventoryDTO{
    id:string 
    title: string
    description:string
    imageUrl: string
    genres: string[]
}

export interface Preferences{
    visibility: string
    genres:string[]
}

export interface ProfileResponseDTO{
    id:string
    fullName:string
    username:string
    location:string
    profilePicture:string
    friendCount:number
    groupCount:number
    ownedGameCount:number
    games:GameInventoryDTO[]
    communities: Map<string,string>[]
    status: FriendStatus
    preferences: Preferences
    createdAt: string
}

export interface FriendShip{
    id:string
    sender:string
    status: FriendStatus
    createdAt:string
}

export const FriendService = {

    // GET /api/users/friends
    getOwnFriendsList() {
        const { $api } = useNuxtApp()
        return $api<FriendListDTO>('users/friends', {
            method: 'GET'
        })
    },

    // GET /api/users/{userId}/friends
    getUserFriendsList(userId: string) {
        const { $api } = useNuxtApp()
        return $api<FriendListDTO>(`users/${userId}/friends`, {
            method: 'GET'
        })
    },

    // GET /api/users/friendRequests
    getFriendRequests() {
        const { $api } = useNuxtApp()
        return $api<FriendRequestsDTO>('users/friendRequests', {
            method: 'GET'
        })
    },

    // POST /api/users/{userId}/friendRequests
    sendFriendRequest(userId: string) {
        const { $api } = useNuxtApp()
        return $api<FriendRequestResponse>(`users/${userId}/friendRequests`, {
            method: 'POST'
        })
    },

    // PATCH /api/users/friendRequests/{requestId}?status=accept|decline
    respondToFriendRequest(requestId: string, status: 'accept' | 'decline') {
        const { $api } = useNuxtApp()
        return $api<FriendRequestResponse>(`users/friendRequests/${requestId}`, {
            method: 'PATCH',
            query: { status }
        })
    },

    // DELETE /api/users/friends/{userId}
    unfriendUser(userId: string) {
        const { $api } = useNuxtApp()
        return $api<FriendRequestResponse>(`users/friends/${userId}`, {
            method: 'DELETE'
        })
    },

    // GET /api/users/notifications
    getMissedNotifications() {
        const { $api } = useNuxtApp()
        return $api<NotificationsDTO>('users/notifications', {
            method: 'GET'
        })
    },

    //GET /api/{userId}
    getOtherUserProfile(userId: string){
        const{ $api } = useNuxtApp();
        return $api<ProfileResponseDTO>(`/${userId}`,{
            method: 'GET'
        });
    },

    getOtherUserFriendsList(userId:string){
        const { $api } = useNuxtApp();
        return $api<FriendListDTO>(`/${userId}/friends`, {
            method: 'GET'
        });
    }
    
}
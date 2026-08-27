package com.boardwise.backend.user_service.models;

public sealed interface NotificationData 
permits ChatMessageData, EventInviteData, FriendRequestData, 
FriendConfirmationData{}

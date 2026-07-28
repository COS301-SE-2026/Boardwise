import { profile, appearance, privacy, notifications } from "./mockData/settings"

export const getProfile = () => ({
    ...profile
})

export const getAppearance = () => ({
    ...appearance
})

export const getPrivacy = () => ({
    visibility: privacy.visibility,
    settings: {
        ...privacy.settings
    }
})

export const getNotification = () => ({
    ...notifications
})
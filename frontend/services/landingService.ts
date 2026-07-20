import { featuredGames, platformFeatures } from './mockData/landing'

export const getFeaturedGames = () => {
    return [...featuredGames]
}

export const getPlatformFeatures = () => {
    return [...platformFeatures]
}
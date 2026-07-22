import { onboardingSteps, platformFeatures } from './mockData/landing'
import { games } from './mockData/games'

export const getOnBoardingSteps = () => {
    return [...onboardingSteps]
}

export const getPlatformFeatures = () => {
    return [...platformFeatures]
}

export const getLandingGames = () => {
    return [...games]
}
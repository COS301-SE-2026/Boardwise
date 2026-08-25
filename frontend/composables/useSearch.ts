import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'
import { MarketplaceService, type ListingResponse } from '~/services/marketplaceService'
import { CommunityService, type GroupInfo } from '~/services/communityService'
import { userService, type ProfileSearchResponse } from '~/services/userService'
import { useSnackBar } from './useSnackbar'

// TODO: Fix any issues : integration
const { show } = useSnackBar()

export interface RulebookCardData {
    id: string
    title: string
    coverUrl: string
    genre: string
}

export interface ListingCardData {
    id: string
    title: string
    price: number
    imageUrl: string | null
}

export interface CommunityCardData {
    id: string
    name: string
    description: string
    imageUrl: string
    visibility: string
    memberCount: number
}

export interface PersonCardData {
    id: string
    username: string
    mutualLabel: string
    isFriend: boolean
    avatarUrl: string | null
}

export const useSearch = () => {
    const people = ref<PersonCardData[]>([])
    const rulebooks = ref<RulebookCardData[]>([])
    const listings = ref<ListingCardData[]>([])
    const communities = ref<CommunityCardData[]>([])

    const loading = ref<boolean>(false)
    const error = ref<string>('')

    // Mock 
    // TODO: Fix this usage
    const mockSearchForUser = async (query: string): Promise<ProfileSearchResponse[]> => {
        return [
            { id: '1', username: 'catan_carla', fullName: 'Carla Santos', profilePicture: '' },
            { id: '2', username: 'settlersfan88', fullName: 'Sam Ellis', profilePicture: '' },
        ].filter(p => p.username.toLowerCase().includes(query.toLowerCase()))
    }

    const fetchPeople = async (query: string) => {
        const res = await userService.searchForUser(query) as ProfileSearchResponse[]
        people.value = res.map((p): PersonCardData => ({
            id: p.id,
            username: p.username,
            //TODO: Fix friends part : backend
            mutualLabel: '', 
            isFriend: false, 
            avatarUrl: p.profilePicture || null,
        }))
    }
    
    const fetchRulebooks = async (query: string) => {
        const res = await LibraryService.fetchAllRulebooks({ title: query })
        rulebooks.value = (res?.content ?? []).map((rb): RulebookCardData => ({
            id: rb.id,
            title: rb.title,
            coverUrl: rb.coverUrl,
            genre: rb.genres?.[0] ?? '',
        }))
    }

    const fetchListingResults = async (query: string) => {
        const res = await MarketplaceService.getListings({ search: query })
        const raw = (res?.content ?? res ?? []) as ListingResponse[]
        listings.value = raw.map((l): ListingCardData => ({
            id: l.listingId,
            title: l.listingTitle,
            price: l.price,
            imageUrl: l.imageUrl ?? null,
        }))
    }

    const fetchCommunities = async (query: string) => {
        const res = await CommunityService.searchForCommunity(query) as GroupInfo[]
        communities.value = res.map((c): CommunityCardData => ({
            id: c.id,
            name: c.name,
            description: c.description,
            imageUrl: c.imageUrl,
            visibility: c.visibility,
            memberCount: c.memberCount,
        }))
    }

    const search = async (query: string) => {
        error.value = ''

        if (!query) {
            people.value = []
            rulebooks.value = []
            listings.value = []
            communities.value = []
            return
        }

        loading.value = true

        const results = await Promise.allSettled([
            fetchPeople(query),
            fetchRulebooks(query),
            fetchListingResults(query),
            fetchCommunities(query),
        ])

        const failed = results.filter(r => r.status === 'rejected')
        if (failed.length) {
            error.value = 'Some results could not be loaded'
            show('Some search results failed to load', 'error')
            failed.forEach(r => console.error('Search section failed:', (r as PromiseRejectedResult).reason))
        }

        loading.value = false
    }

    return { people, rulebooks, listings, communities, loading, error, search }
}
import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'
import { MarketplaceService, type ListingResponse } from '~/services/marketplaceService'
import { CommunityService } from '~/services/communityService'
import { FriendStatus, userService, type ProfileSearchResponse } from '~/services/userService'
import { EventService } from '~/services/eventService'


export interface RulebookCardData {
    id: string
    title: string
    coverUrl: string
    genre: string
}

export interface ListingCardData {
    id: string
    listingTitle: string
    gameTitle: string
    username: string;
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

export interface EventCardData {
    id: string
    name: string
    imageUrl: string | null
    startTime: string
    location: string
    eventStatus: string
}

let requestId = 0

export const useSearch = () => {
    const people = ref<PersonCardData[]>([])
    const rulebooks = ref<RulebookCardData[]>([])
    const listings = ref<ListingCardData[]>([])
    const communities = ref<CommunityCardData[]>([])
    const events = ref<EventCardData[]>([])

    const loading = ref<boolean>(false)
    const error = ref<string>('')


    const fetchPeople = async (q: string): Promise<PersonCardData[]> => {
        const raw: any = await userService.searchForUser(q)
        const list: ProfileSearchResponse[] = Array.isArray(raw) ? raw : raw?.users ?? []
        return list.map(p => ({
            id: p.id,
            username: p.username, 
            mutualLabel: '',
            isFriend: p.status === FriendStatus.ACCEPTED,
            avatarUrl: p.profilePicture || null,
        }))
    }
    
    const fetchRulebooks = async (q: string): Promise<RulebookCardData[]> => {
        const res = await LibraryService.fetchAllRulebooks({ search: q })
        return (res?.content ?? []).map(rb => ({
            id: rb.id, title: rb.title, coverUrl: rb.coverUrl, genre: rb.genres?.[0] ?? '',
        }))
    }

    const fetchListingResults = async (q: string): Promise<ListingCardData[]> => {
        const res = await MarketplaceService.getListings({ search: q })
        const raw = (res?.content ?? res ?? []) as ListingResponse[]
        return raw.map(l => ({
            id: l.listingId, gameTitle: l.gameTitle, listingTitle: l.listingTitle,
            price: l.price, imageUrl: l.imageUrl ?? null, username: l.username,
        }))
    }

    const fetchCommunities = async (q: string): Promise<CommunityCardData[]> => {
        const res = (await CommunityService.searchForCommunity(q)) as any[]
        return res.map(c => ({
            id: c.id ?? c.groupId, name: c.name, description: c.description, 
            imageUrl: c.imageUrl, visibility: c.visibility, memberCount: c.memberCount,
        }))
    }

    const fetchEvents = async (q: string): Promise<EventCardData[]> => {
        const res = await EventService.getAllEvents(q)
        return (res?.result ?? []).map(e => ({
            id: e.id,
            name: e.name,
            imageUrl: e.imageUrl,
            startTime: e.startTime,
            location: e.location,
            eventStatus: e.eventStatus,
        }))
    }

    const search = async (query: string) => {
        const q = query.trim()
        const id = ++requestId
        error.value = ''

        if (!q) {
            people.value = [];
            rulebooks.value = [];
            listings.value = [];
            communities.value = [];
            events.value = [];
            loading.value = false;
            return
        }

        loading.value = true

        const [p, r, l, c, e] = await Promise.allSettled([
            fetchPeople(q), fetchRulebooks(q), fetchListingResults(q), fetchCommunities(q),fetchEvents(q),
        ])

        if(id !== requestId) return

        people.value = p.status === 'fulfilled' ? p.value : []
        rulebooks.value = r.status === 'fulfilled' ? r.value : []
        listings.value = l.status === 'fulfilled' ? l.value : []
        communities.value = c.status === 'fulfilled' ? c.value : []
        events.value = e.status === 'fulfilled' ? e.value : []

        const failed = [ p, r, l, c, e].filter(x => x.status === 'rejected') as PromiseRejectedResult[]
        if(failed.length) {
            error.value = 'Some results could not be loaded'
            failed.forEach(f => console.error('Search section failed:', f.reason))
        }

        loading.value = false
    }

    return { people, rulebooks, listings, communities, events, loading, error, search }
}
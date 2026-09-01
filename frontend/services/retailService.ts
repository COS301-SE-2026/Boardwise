export interface RetailListings {
    retailer: string
    retailTitle: string
    price: number
    imageUrl: string
    url: string
    JaroWinklerSimilarityScore: number
}

export interface sortJsonStructure {
    empty: boolean
    sorted: boolean
    unsorted: boolean
}

export interface pageableStructure {
    sort: sortJsonStructure
    offset: number
    pageNumber: number
    pageSize: number
    paged: boolean
    unpaged: boolean
}

export interface PageImplRetailPage {
    content: RetailListings[]
    pageable: pageableStructure
    last: boolean
    totalElements: number
    size: number
    number: number
    sort: sortJsonStructure
    numberOfElements: number
    first: boolean
    empty: boolean
}

export const RetailService = {
    // GET PERSONALISED LISTINGS
    getPersonalisedListings(page: number) {
        const { $api } = useNuxtApp()
        return $api<PageImplRetailPage>(`marketplace/listings/personalised`, {
            method: 'GET',
            query: { page }
        })
    }
}